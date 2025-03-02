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

package bisq.desktop.primary.main.content.chat;

import bisq.chat.channel.Channel;
import bisq.chat.message.ChatMessage;
import bisq.desktop.common.view.NavigationModel;
import bisq.desktop.primary.main.content.chat.sidebar.UserProfileSidebar;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Getter
public abstract class BaseChatModel extends NavigationModel {
    private final Map<String, StringProperty> chatMessagesByChannelId = new HashMap<>();
    private final StringProperty selectedChatMessages = new SimpleStringProperty("");
    private final StringProperty selectedChannelAsString = new SimpleStringProperty("");
    private final ObjectProperty<Channel<? extends ChatMessage>> selectedChannel = new SimpleObjectProperty<>();
    private final ObjectProperty<Pane> chatUserDetailsRoot = new SimpleObjectProperty<>();
    private final BooleanProperty sideBarVisible = new SimpleBooleanProperty();
    private final BooleanProperty sideBarChanged = new SimpleBooleanProperty();
    private final DoubleProperty sideBarWidth = new SimpleDoubleProperty();
    private final BooleanProperty channelInfoVisible = new SimpleBooleanProperty();
    private final ObjectProperty<Node> channelIcon = new SimpleObjectProperty<>();
    private final StringProperty searchText = new SimpleStringProperty();
    @Setter
    private Optional<UserProfileSidebar> chatUserDetails = Optional.empty();

    public BaseChatModel() {
    }
}
