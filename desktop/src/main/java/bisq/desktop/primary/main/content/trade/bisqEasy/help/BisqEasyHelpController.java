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

package bisq.desktop.primary.main.content.trade.bisqEasy.help;

import bisq.application.DefaultApplicationService;
import bisq.desktop.common.view.Controller;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BisqEasyHelpController implements Controller {
    protected final BisqEasyHelpModel model;
    @Getter
    protected final BisqEasyHelpView view;

    public BisqEasyHelpController(DefaultApplicationService applicationService) {
        model = getGenerateProfileModel();
        view = getGenerateProfileView();
    }

    protected BisqEasyHelpView getGenerateProfileView() {
        return new BisqEasyHelpView(model, this);
    }

    protected BisqEasyHelpModel getGenerateProfileModel() {
        return new BisqEasyHelpModel();
    }

    @Override
    public void onActivate() {
    }

    @Override
    public void onDeactivate() {
    }
}
