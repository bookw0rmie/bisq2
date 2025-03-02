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

package bisq.chat.message;

import bisq.chat.channel.ChannelDomain;
import bisq.network.p2p.services.data.storage.MetaData;
import bisq.network.p2p.services.data.storage.mailbox.MailboxMessage;
import bisq.user.profile.UserProfile;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

/**
 * PrivateChatMessage is sent as direct message to peer and in case peer is not online it can be stores as
 * mailbox message.
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BasePrivateChatMessage extends ChatMessage implements MailboxMessage {
    protected final String receiversId;
    protected final UserProfile sender;

    protected BasePrivateChatMessage(String messageId,
                                     ChannelDomain channelDomain,
                                     String channelName,
                                     UserProfile sender,
                                     String receiversId,
                                     String text,
                                     Optional<Quotation> quotedMessage,
                                     long date,
                                     boolean wasEdited,
                                     MessageType messageType,
                                     MetaData metaData) {
        super(messageId,
                channelDomain,
                channelName,
                sender.getId(),
                Optional.of(text),
                quotedMessage,
                date,
                wasEdited,
                messageType,
                metaData);
        this.receiversId = receiversId;
        this.sender = sender;
    }

    @Override
    public MetaData getMetaData() {
        return metaData;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - getDate() > getMetaData().getTtl());
    }
}