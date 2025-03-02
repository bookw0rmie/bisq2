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

package bisq.desktop.primary.main.content.trade.bisqEasy.chat;

import bisq.chat.channel.ChannelDomain;
import bisq.desktop.common.view.NavigationTarget;
import bisq.desktop.primary.main.content.chat.BaseChatModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class BisqEasyChatModel extends BaseChatModel {
    private final BooleanProperty offerOnly = new SimpleBooleanProperty();
    private final ChannelDomain channelDomain;
    private final StringProperty actionButtonText = new SimpleStringProperty();
    private final BooleanProperty createOfferButtonVisible = new SimpleBooleanProperty();
    private final BooleanProperty openDisputeDisabled = new SimpleBooleanProperty();
    private final BooleanProperty tradeHelpersVisible = new SimpleBooleanProperty();
    private final BooleanProperty completeTradeDisabled = new SimpleBooleanProperty();
    private final StringProperty completeTradeTooltip = new SimpleStringProperty();

    public BisqEasyChatModel(ChannelDomain channelDomain) {
        this.channelDomain = channelDomain;
    }

    @Override
    public NavigationTarget getDefaultNavigationTarget() {
        return NavigationTarget.NONE;
    }
}
