/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.network.p2p.services.data.storage.mailbox;

import bisq.common.data.ByteArray;
import bisq.network.p2p.services.data.storage.DataStorageService;
import bisq.network.p2p.services.data.storage.DataStore;
import bisq.network.p2p.services.data.storage.Result;
import bisq.persistence.PersistenceService;
import bisq.security.DigestUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class MailboxDataStorageService extends DataStorageService<MailboxRequest> {
    private static final long MAX_AGE = TimeUnit.DAYS.toMillis(10);
    private static final int MAX_MAP_SIZE = 10000;

    public interface Listener {
        void onAdded(MailboxData mailboxData);

        void onRemoved(MailboxData mailboxData);
    }

    private final Set<Listener> listeners = new CopyOnWriteArraySet<>();
    private final Object mapAccessLock = new Object();

    public MailboxDataStorageService(PersistenceService persistenceService, String storeName, String fileName) {
        super(persistenceService, storeName, fileName);
    }

    @Override
    public void onPersistedApplied(DataStore<MailboxRequest> persisted) {
        maybePruneMap(persisted.getMap());
    }

    @Override
    protected long getMaxWriteRateInMs() {
        return 1000;
    }

    public Result add(AddMailboxRequest request) {
        MailboxSequentialData mailboxSequentialData = request.getMailboxSequentialData();
        MailboxData mailboxData = mailboxSequentialData.getMailboxData();
        byte[] hash = DigestUtil.hash(mailboxData.serialize());
        ByteArray byteArray = new ByteArray(hash);
        MailboxRequest requestFromMap;
        Map<ByteArray, MailboxRequest> map = persistableStore.getMap();
        synchronized (mapAccessLock) {
            if (map.size() > MAX_MAP_SIZE) {
                return new Result(false).maxMapSizeReached();
            }
            requestFromMap = map.get(byteArray);
            int sequenceNumberFromMap = requestFromMap != null ? requestFromMap.getSequenceNumber() : 0;

            if (request.equals(requestFromMap)) {
                return new Result(false).requestAlreadyReceived();
            }

            if (requestFromMap != null && mailboxSequentialData.isSequenceNrInvalid(sequenceNumberFromMap)) {
                return new Result(false).sequenceNrInvalid();
            }

            if (mailboxSequentialData.isExpired()) {
                return new Result(false).expired();
            }

            if (mailboxData.isDataInvalid(mailboxSequentialData.getSenderPublicKeyHash())) {
                return new Result(false).dataInvalid();
            }

            if (request.isPublicKeyInvalid()) {
                return new Result(false).publicKeyHashInvalid();
            }

            if (request.isSignatureInvalid()) {
                return new Result(false).signatureInvalid();
            }
            map.put(byteArray, request);
        }
        persist();

        // If we had already the data (only updated seq nr) we return false as well and do not notify listeners.
        // This should only happen if client re-publishes mailbox data 
        if (requestFromMap != null) {
            return new Result(false).payloadAlreadyStored();
        }

        listeners.forEach(listener -> listener.onAdded(mailboxData));
        return new Result(true);
    }

    public Result remove(RemoveMailboxRequest request) {
        ByteArray byteArray = new ByteArray(request.getHash());
        Map<ByteArray, MailboxRequest> map = persistableStore.getMap();
        MailboxRequest requestFromMap = map.get(byteArray);
        MailboxSequentialData sequentialSataFromMap;
        synchronized (mapAccessLock) {
            if (requestFromMap == null) {
                // We don't have any entry, but it might be that we would receive later an add request, so we need to keep
                // track of the sequence number
                map.put(byteArray, request);
                persist();
                return new Result(false).noEntry();
            }

            if (requestFromMap instanceof RemoveMailboxRequest) {
                // We have had the entry already removed.
                if (!request.isSequenceNrInvalid(requestFromMap.getSequenceNumber())) {
                    // We update the request, so we have the latest sequence number.
                    map.put(byteArray, request);
                    persist();
                }
                return new Result(false).alreadyRemoved();
            }

            // At that point we know requestFromMap is an AddProtectedDataRequest
            AddMailboxRequest addRequest = (AddMailboxRequest) requestFromMap;
            // We have an entry, lets validate if we can remove it
            sequentialSataFromMap = addRequest.getMailboxSequentialData();
            if (request.isSequenceNrInvalid(sequentialSataFromMap.getSequenceNumber())) {
                // Sequence number has not increased
                return new Result(false).sequenceNrInvalid();
            }

            if (request.isPublicKeyHashInvalid(sequentialSataFromMap)) {
                // Hash of pubKey of data does not match provided one
                return new Result(false).publicKeyHashInvalid();
            }

            if (request.isSignatureInvalid()) {
                return new Result(false).signatureInvalid();
            }

            map.put(byteArray, request);
            listeners.forEach(listener -> listener.onRemoved(sequentialSataFromMap.getMailboxData()));
        }

        persist();
        return new Result(true).removedData(sequentialSataFromMap.getMailboxData());
    }

    @Override
    public void shutdown() {
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    int getSequenceNumber(byte[] hash) {
        ByteArray byteArray = new ByteArray(hash);
        int sequenceNumber = 0;
        Map<ByteArray, MailboxRequest> map = persistableStore.getMap();
        synchronized (mapAccessLock) {
            if (map.containsKey(byteArray)) {
                sequenceNumber = map.get(byteArray).getSequenceNumber();
            }
        }
        return sequenceNumber;
    }

    boolean contains(byte[] hash) {
        Map<ByteArray, MailboxRequest> map = persistableStore.getMap();
        synchronized (mapAccessLock) {
            return map.containsKey(new ByteArray(hash));
        }
    }

    boolean canAddMailboxMessage(MailboxData mailboxData) {
        byte[] hash = DigestUtil.hash(mailboxData.serialize());
        return getSequenceNumber(hash) < Integer.MAX_VALUE;
    }

    private void maybePruneMap(Map<ByteArray, MailboxRequest> persisted) {
        long now = System.currentTimeMillis();
        // Remove entries older than MAX_AGE
        // Remove expired ProtectedEntry in case value is of type AddProtectedDataRequest
        // Sort by created date
        // Limit to MAX_MAP_SIZE
        Map<ByteArray, MailboxRequest> pruned = persisted.entrySet().stream()
                .filter(entry -> now - entry.getValue().getCreated() < MAX_AGE)
                .filter(entry -> entry.getValue() instanceof RemoveMailboxRequest ||
                        !((AddMailboxRequest) entry.getValue()).getMailboxSequentialData().isExpired())
                .sorted((o1, o2) -> Long.compare(o2.getValue().getCreated(), o1.getValue().getCreated()))
                .limit(MAX_MAP_SIZE)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<ByteArray, MailboxRequest> map = persistableStore.getMap();
        synchronized (mapAccessLock) {
            map.clear();
            map.putAll(pruned);
        }
    }
}
