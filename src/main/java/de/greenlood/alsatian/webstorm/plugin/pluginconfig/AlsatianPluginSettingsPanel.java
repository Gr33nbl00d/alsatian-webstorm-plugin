/*
 * Created by JFormDesigner on Thu Jul 16 13:18:46 CEST 2020
 */

package de.greenlood.alsatian.webstorm.plugin.pluginconfig;

import com.intellij.openapi.options.ConfigurationException;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author User #10
 */
public class AlsatianPluginSettingsPanel extends JPanel {
    public AlsatianPluginSettingsPanel() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        lblDefaultDirWildcard = new JLabel();
        txtDefaultDirWildcard = new JTextField();
        lblPathToNYC = new JLabel();
        txtPathToNYC = new JTextField();

        //======== this ========
        setLayout(new MigLayout(
                "hidemode 3",
                // columns
                "[fill]" +
                        "[250,fill]",
                // rows
                "[]" +
                        "[]" +
                        "[]"));

        //---- lblDefaultDirWildcard ----
        lblDefaultDirWildcard.setText("Default Directory Wildcard");
        add(lblDefaultDirWildcard, "cell 0 0");
        add(txtDefaultDirWildcard, "cell 1 0");

        //---- lblPathToNYC ----
        lblPathToNYC.setText("Path to NYC ( for coverage)");
        add(lblPathToNYC, "cell 0 1");
        add(txtPathToNYC, "cell 1 1");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public void apply(AlsatianPluginSettingsState settings) throws ConfigurationException {
        settings.nycPackage = txtPathToNYC.getText();
        settings.defaultDirectoryWildcard = txtDefaultDirWildcard.getText();
        Path path = Paths.get(settings.project.getBasePath(), txtPathToNYC.getText(), "bin/nyc.js");
        if (path.toFile().exists() == false) {
            throw new ConfigurationException("NYC not found in path: " + path.toString());
        }
        if (path.toFile().isFile() == false) {
            throw new ConfigurationException("nyc.js is not a file: " + path.toString());
        }

    }

    public boolean isModified(AlsatianPluginSettingsState settings) {
        boolean b = !txtDefaultDirWildcard.getText().equals(settings.defaultDirectoryWildcard);
        b = b || !txtPathToNYC.getText().equals(settings.nycPackage);
        return b;
    }

    public void reset(AlsatianPluginSettingsState settings) {
        this.txtPathToNYC.setText(settings.getState().nycPackage);
        this.txtDefaultDirWildcard.setText(settings.getState().defaultDirectoryWildcard);
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel lblDefaultDirWildcard;
    private JTextField txtDefaultDirWildcard;
    private JLabel lblPathToNYC;
    private JTextField txtPathToNYC;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
