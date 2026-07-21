package org.IsmaelSS.view;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

import java.util.List;
import org.IsmaelSS.model.Theme;

public class ThemeSelectionView {
    private final Scene scene;
    private final TabPane root;
    private final Tab jogarTab;
    private final Tab relatoriosTab;
    private final Tab gerenciarTab;
    private ReviewDashboardView dashboardView;

    public ThemeSelectionView() {
        jogarTab = new Tab("Treinar", new VBox());
        jogarTab.setClosable(false);

        relatoriosTab = new Tab("Relatórios");
        relatoriosTab.setClosable(false);

        gerenciarTab = new Tab("Gerenciar");
        gerenciarTab.setClosable(false);

        root = new TabPane(jogarTab, relatoriosTab, gerenciarTab);
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        scene = new Scene(root);
    }

    public Scene getScene() { return scene; }
    public TabPane getTabPane() { return root; }
    public Tab getJogarTab() { return jogarTab; }
    public Tab getRelatoriosTab() { return relatoriosTab; }
    public Tab getGerenciarTab() { return gerenciarTab; }

    public void setDashboard(ReviewDashboardView dv) {
        this.dashboardView = dv;
        jogarTab.setContent(dv.getContent());
    }

    public ReviewDashboardView getDashboard() { return dashboardView; }

    public void refreshDashboard(List<Theme> themes) {
        if (dashboardView != null) {
            // Rebuild will be called by controller
        }
    }
}
