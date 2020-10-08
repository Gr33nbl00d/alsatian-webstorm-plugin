/*
 * Created by JFormDesigner on Fri Feb 21 12:44:02 CET 2020
 */

package de.greenlood.alsatian.webstorm.plugin.runconfig;


import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.util.NodePackageField;
import com.intellij.javascript.testFramework.AbstractTestStructureElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.SwingHelper;
import com.intellij.webcore.ui.PathShortener;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestFileExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestMethodExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianTestSuiteExecutionPlan;
import de.greenlood.alsatian.webstorm.plugin.filestructure.AlsatianFileStructure;
import de.greenlood.alsatian.webstorm.plugin.filestructure.AlsatianFileStructureBuilder;
import de.greenlood.alsatian.webstorm.plugin.filestructure.AlsatianSpecStructure;
import de.greenlood.alsatian.webstorm.plugin.filestructure.AlsatianSuiteStructure;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


/**
 * @author unknown
 */
public class AlsatianRunSettingsPanel extends JPanel {
    private final JBList<String> listOfGlobs;
    private NodeJsInterpreterField interpreterField;
    private NodePackageField alsatianPackageField;
    private AlsatianPackageDescriptorFactory alsatianPackageDescriptorFactory = new AlsatianPackageDescriptorFactory();
    private Project project;
    private AlsatianScopeKind currentScopeKind;

    public AlsatianRunSettingsPanel() {
        initComponents();
        this.listOfGlobs = new JBList<>();
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(listOfGlobs);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                String newWildcard = Messages.showInputDialog("Enter Alsatian Wildcard like ./src/**/*.spec.ts", "New Wildcard", null);
                DefaultListModel<String> model = (DefaultListModel<String>) listOfGlobs.getModel();
                model.addElement(newWildcard);
            }
        });
        this.pListOfGlobs.add(decorator.createPanel(),BorderLayout.CENTER);
    }

    private void rbAllTestsActionPerformed(ActionEvent e) {
        setScopeKind(AlsatianScopeKind.WILDCARD);
    }

    private void rbTestFileActionPerformed(ActionEvent e) {
        setScopeKind(AlsatianScopeKind.TEST_FILE);
    }


    private void rbSuiteActionPerformed(ActionEvent e) {
        setScopeKind(AlsatianScopeKind.SUITE);
        if (isValidTestFile(txtTestFile.getText())) {
            AlsatianFileStructure alsatianFileStructure = readTestFileStructure(txtTestFile.getText());
            fillCbSuites(alsatianFileStructure);
        }
    }

    private void rbTestActionPerformed(ActionEvent e) {
        AlsatianScopeKind oldScopeKind = currentScopeKind;
        setScopeKind(AlsatianScopeKind.TEST);
        if (oldScopeKind == AlsatianScopeKind.SUITE) {
            if (isValidTestFile(txtTestFile.getText())) {
                AlsatianFileStructure alsatianFileStructure = readTestFileStructure(txtTestFile.getText());
                AlsatianSuiteStructure suite = alsatianFileStructure.findSuiteByName((String) cbTestSuite.getSelectedItem());
                fillCbTestMethods(suite);
            }
        }
        if (oldScopeKind == AlsatianScopeKind.TEST_FILE) {
            if (isValidTestFile(txtTestFile.getText())) {
                AlsatianFileStructure alsatianFileStructure = readTestFileStructure(txtTestFile.getText());
                fillCbSuites(alsatianFileStructure);
                fillCbTestMethods(null);
            }
        }
    }

    private void txtTestFile2ActionPerformed(ActionEvent e) {
        if (isValidTestFile(txtTestFile.getText())) {
            AlsatianFileStructure alsatianFileStructure = readTestFileStructure(txtTestFile.getText());
            fillCbSuites(alsatianFileStructure);
            cbTestSuiteActionPerformed(e);
        }
    }

    private boolean isValidTestFile(String text) {
        if(text==null)
            return false;
        VirtualFile virtualTestFile = getVirtualFileByRelativePath(text);
        if(virtualTestFile==null)
            return false;
        return virtualTestFile.isDirectory() == false && virtualTestFile.isValid();
    }

    private VirtualFile getVirtualFileByRelativePath(String text) {
        VirtualFile[] vFiles = ProjectRootManager.getInstance(this.project).getContentRoots();
        for (VirtualFile vFile : vFiles) {
            VirtualFile child = vFile.findFileByRelativePath(FileUtil.toSystemIndependentName(text));
            if (child != null)
                return child;
        }
        return null;
    }

    private void cbTestSuiteActionPerformed(ActionEvent e) {
        AlsatianFileStructure alsatianFileStructure = readTestFileStructure(txtTestFile.getText());
        String selectedSuite = (String) cbTestSuite.getSelectedItem();
        AlsatianSuiteStructure suite = null;
        if (selectedSuite != null) {
            suite = alsatianFileStructure.findSuiteByName(selectedSuite);
        }
        fillCbTestMethods(suite);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        pGeneralSettings = new JPanel();
        lblNodeInterpreter = new JLabel();
        pNodeInterpreter = new JPanel();
        lblNodeOptions = new JLabel();
        txtNodeOptions = new RawCommandLineEditor();
        lblAlsatianPackage = new JLabel();
        pAlsatianPackage = new JPanel();
        lblWorkingDir = new JLabel();
        txtWorkingDir = new TextFieldWithBrowseButton();
        lblEnvVar = new JLabel();
        envVarComponent = new EnvironmentVariablesTextFieldWithBrowseButton();
        rbAllTests = new JRadioButton();
        rbTestFile = new JRadioButton();
        rbSuite = new JRadioButton();
        rbTest = new JRadioButton();
        pTest = new JPanel();
        lblTestFile = new JLabel();
        txtTestFile = new TextFieldWithBrowseButton();
        lblTestSuite = new JLabel();
        cbTestSuite = new JComboBox();
        lblTestMethod = new JLabel();
        cbTestMethod = new JComboBox();
        pListOfGlobs = new JPanel();

        //======== this ========
        setLayout(new MigLayout(
            "insets 0,hidemode 3,gap 5 5",
            // columns
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[grow,fill]",
            // rows
            "[]" +
            "[fill]" +
            "[fill]" +
            "[]"));

        //======== pGeneralSettings ========
        {
            pGeneralSettings.setLayout(new MigLayout(
                "hidemode 3",
                // columns
                "[fill]" +
                "[grow,fill]",
                // rows
                "[]" +
                "[]" +
                "[]" +
                "[]" +
                "[]"));

            //---- lblNodeInterpreter ----
            lblNodeInterpreter.setText("Node interpreter:");
            pGeneralSettings.add(lblNodeInterpreter, "cell 0 0");

            //======== pNodeInterpreter ========
            {
                pNodeInterpreter.setLayout(new BorderLayout());
            }
            pGeneralSettings.add(pNodeInterpreter, "cell 1 0");

            //---- lblNodeOptions ----
            lblNodeOptions.setText("Node options:");
            pGeneralSettings.add(lblNodeOptions, "cell 0 1");
            pGeneralSettings.add(txtNodeOptions, "cell 1 1");

            //---- lblAlsatianPackage ----
            lblAlsatianPackage.setText("Alsatian package:");
            pGeneralSettings.add(lblAlsatianPackage, "cell 0 2");

            //======== pAlsatianPackage ========
            {
                pAlsatianPackage.setLayout(new BorderLayout());
            }
            pGeneralSettings.add(pAlsatianPackage, "cell 1 2");

            //---- lblWorkingDir ----
            lblWorkingDir.setText("Working directory:");
            pGeneralSettings.add(lblWorkingDir, "cell 0 3");
            pGeneralSettings.add(txtWorkingDir, "cell 1 3");

            //---- lblEnvVar ----
            lblEnvVar.setText("Environment variables:");
            lblEnvVar.setIcon(null);
            pGeneralSettings.add(lblEnvVar, "cell 0 4");
            pGeneralSettings.add(envVarComponent, "cell 1 4");
        }
        add(pGeneralSettings, "cell 0 0 5 1");

        //---- rbAllTests ----
        rbAllTests.setText("Wildcard");
        rbAllTests.addActionListener(e -> rbAllTestsActionPerformed(e));
        add(rbAllTests, "cell 0 1");

        //---- rbTestFile ----
        rbTestFile.setText("Test File");
        rbTestFile.addActionListener(e -> rbTestFileActionPerformed(e));
        add(rbTestFile, "cell 1 1");

        //---- rbSuite ----
        rbSuite.setText("Fixture");
        rbSuite.addActionListener(e -> rbSuiteActionPerformed(e));
        add(rbSuite, "cell 2 1");

        //---- rbTest ----
        rbTest.setText("Test");
        rbTest.addActionListener(e -> rbTestActionPerformed(e));
        add(rbTest, "cell 3 1");

        //======== pTest ========
        {
            pTest.setLayout(new MigLayout(
                "hidemode 3",
                // columns
                "[fill]" +
                "[fill]" +
                "[grow,fill]",
                // rows
                "[]" +
                "[]" +
                "[]" +
                "[]" +
                "[]"));

            //---- lblTestFile ----
            lblTestFile.setText("Test file:");
            pTest.add(lblTestFile, "cell 0 0");

            //---- txtTestFile ----
            txtTestFile.addActionListener(e -> txtTestFile2ActionPerformed(e));
            pTest.add(txtTestFile, "cell 1 0");

            //---- lblTestSuite ----
            lblTestSuite.setText("Test Suite");
            pTest.add(lblTestSuite, "cell 0 1");

            //---- cbTestSuite ----
            cbTestSuite.addActionListener(e -> cbTestSuiteActionPerformed(e));
            pTest.add(cbTestSuite, "cell 1 1");

            //---- lblTestMethod ----
            lblTestMethod.setText("Test Method");
            pTest.add(lblTestMethod, "cell 0 2");
            pTest.add(cbTestMethod, "cell 1 2");

            //======== pListOfGlobs ========
            {
                pListOfGlobs.setLayout(new BorderLayout());
            }
            pTest.add(pListOfGlobs, "cell 0 3 3 1");
        }
        add(pTest, "cell 0 2 5 1");

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(rbAllTests);
        buttonGroup1.add(rbTestFile);
        buttonGroup1.add(rbSuite);
        buttonGroup1.add(rbTest);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel pGeneralSettings;
    private JLabel lblNodeInterpreter;
    private JPanel pNodeInterpreter;
    private JLabel lblNodeOptions;
    private RawCommandLineEditor txtNodeOptions;
    private JLabel lblAlsatianPackage;
    private JPanel pAlsatianPackage;
    private JLabel lblWorkingDir;
    private TextFieldWithBrowseButton txtWorkingDir;
    private JLabel lblEnvVar;
    private EnvironmentVariablesTextFieldWithBrowseButton envVarComponent;
    private JRadioButton rbAllTests;
    private JRadioButton rbTestFile;
    private JRadioButton rbSuite;
    private JRadioButton rbTest;
    private JPanel pTest;
    private JLabel lblTestFile;
    private TextFieldWithBrowseButton txtTestFile;
    private JLabel lblTestSuite;
    private JComboBox cbTestSuite;
    private JLabel lblTestMethod;
    private JComboBox cbTestMethod;
    private JPanel pListOfGlobs;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public void init(Project project) {
        this.project = project;

        SwingHelper.installFileCompletionAndBrowseDialog(project, this.txtTestFile, "Select Test File", FileChooserDescriptorFactory.createSingleFileDescriptor());
        PathShortener.enablePathShortening(this.txtTestFile.getTextField(), txtWorkingDir.getTextField());

        SwingHelper.installFileCompletionAndBrowseDialog(project, txtWorkingDir, "Select Working Directory", FileChooserDescriptorFactory.createSingleFolderDescriptor());
        PathShortener.enablePathShortening(this.txtWorkingDir.getTextField(), (JTextField) null);

        this.interpreterField = new NodeJsInterpreterField(project, false);
        this.pNodeInterpreter.add(this.interpreterField);

        Supplier<VirtualFile> directorySupplier = () -> {
            String text = PathShortener.getAbsolutePath(this.txtWorkingDir.getTextField());
            return !text.isEmpty() ? LocalFileSystem.getInstance().findFileByPath(text) : null;
        };
        alsatianPackageField = new NodePackageField(this.interpreterField, alsatianPackageDescriptorFactory.getPackageDescriptor(), directorySupplier);
        this.pAlsatianPackage.add(alsatianPackageField);
    }

    public void resetEditorFrom(AlsatianRunConfiguration runConfiguration) {
        this.interpreterField.setInterpreterRef(runConfiguration.getInterpreterRef());
        this.envVarComponent.setData(runConfiguration.getRunSettings().getEnvData());
        this.alsatianPackageField.setSelected(runConfiguration.getAlsatianPackage());
        AlsatianScopeKind scopeKind = runConfiguration.getRunSettings().getScopeKind();
        this.setScopeKind(scopeKind);
        if (runConfiguration.getRunSettings().getWorkingDir().trim().isEmpty()) {
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(project.getBasePath());
            if (fileByPath != null) {
                String path = fileByPath.getPath();
                File file = new File(path);
                this.txtWorkingDir.setText(file.getAbsolutePath());
            }
        } else {
            this.txtWorkingDir.setText(runConfiguration.getRunSettings().getWorkingDir());
        }
        this.txtNodeOptions.setText(runConfiguration.getRunSettings().getNodeOptions());
        updateEditorVisibility(scopeKind);
        if (scopeKind == AlsatianScopeKind.WILDCARD) {
            DefaultListModel<String> defaultListModel = JBList.createDefaultListModel(runConfiguration.getRunSettings().getListOfGlobs());
            this.listOfGlobs.setModel(defaultListModel);
        } else if (scopeKind == AlsatianScopeKind.TEST_FILE) {
            AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = setTestFileFromConfig(runConfiguration);
        } else if (scopeKind == AlsatianScopeKind.SUITE) {
            AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = setTestFileFromConfig(runConfiguration);
            String path = alsatianTestFileExecutionPlan.getLocationUrl();
            if(isValidTestFile(path)) {
                AlsatianFileStructure alsatianFileStructure = readTestFileStructure(path);
                setTestSuiteFromConfig(alsatianTestFileExecutionPlan, alsatianFileStructure);
            }
        } else if (scopeKind == AlsatianScopeKind.TEST) {
            AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = setTestFileFromConfig(runConfiguration);
            String path = alsatianTestFileExecutionPlan.getLocationUrl();
            if(isValidTestFile(path)) {
                AlsatianFileStructure alsatianFileStructure = readTestFileStructure(path);
                AlsatianTestSuiteExecutionPlan alsatianTestSuiteExecutionPlan = setTestSuiteFromConfig(alsatianTestFileExecutionPlan, alsatianFileStructure);
                String suiteName2 = (String) this.cbTestSuite.getSelectedItem();
                AlsatianSuiteStructure suite = alsatianFileStructure.findSuiteByName(suiteName2);
                fillCbTestMethods(suite);
                AlsatianTestMethodExecutionPlan methodExecutionPlan = alsatianTestSuiteExecutionPlan.getTestMethodExecutionPlans().get(0);
                this.cbTestMethod.setSelectedItem(methodExecutionPlan);
            }
        }

    }

    private void updateEditorVisibility(AlsatianScopeKind scopeKind) {
        if (scopeKind == AlsatianScopeKind.WILDCARD) {
            this.txtTestFile.setVisible(false);
            this.lblTestFile.setVisible(false);
            this.cbTestSuite.setVisible(false);
            this.lblTestSuite.setVisible(false);
            this.cbTestMethod.setVisible(false);
            this.lblTestMethod.setVisible(false);
            this.pListOfGlobs.setVisible(true);
        } else if (scopeKind == AlsatianScopeKind.TEST_FILE) {
            this.txtTestFile.setVisible(true);
            this.lblTestFile.setVisible(true);
            this.cbTestSuite.setVisible(false);
            this.lblTestSuite.setVisible(false);
            this.cbTestMethod.setVisible(false);
            this.lblTestMethod.setVisible(false);
            this.pListOfGlobs.setVisible(false);
        } else if (scopeKind == AlsatianScopeKind.SUITE) {
            this.txtTestFile.setVisible(true);
            this.lblTestFile.setVisible(true);
            this.cbTestSuite.setVisible(true);
            this.lblTestSuite.setVisible(true);
            this.cbTestMethod.setVisible(false);
            this.lblTestMethod.setVisible(false);
            this.pListOfGlobs.setVisible(false);
        } else if (scopeKind == AlsatianScopeKind.TEST) {
            this.txtTestFile.setVisible(true);
            this.lblTestFile.setVisible(true);
            this.cbTestSuite.setVisible(true);
            this.lblTestSuite.setVisible(true);
            this.cbTestMethod.setVisible(true);
            this.lblTestMethod.setVisible(true);
            this.pListOfGlobs.setVisible(false);
        }
    }

    private void fillCbTestMethods(AlsatianSuiteStructure suite) {
        this.cbTestMethod.removeAllItems();
        if (suite != null) {
            List<? extends AbstractTestStructureElement> children = suite.getChildren();
            for (AbstractTestStructureElement child : children) {
                AlsatianSpecStructure alsatianSpecStructure = (AlsatianSpecStructure) child;
                this.cbTestMethod.addItem(alsatianSpecStructure.getName());
            }
        }
    }

    @NotNull
    private AlsatianFileStructure readTestFileStructure(String locationUrl) {
        VirtualFile virtualTestFile = getVirtualFileByRelativePath(locationUrl);
        PsiFile psiTestFile = PsiManager.getInstance(project).findFile(virtualTestFile);
        JSFile jsTestFile = (JSFile) ObjectUtils.tryCast(psiTestFile.getContainingFile(), JSFile.class);
        return AlsatianFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsTestFile);
    }

    @NotNull
    private AlsatianTestSuiteExecutionPlan setTestSuiteFromConfig(AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan, AlsatianFileStructure alsatianFileStructure) {
        fillCbSuites(alsatianFileStructure);
        List<AlsatianTestSuiteExecutionPlan> testSuiteExecutionPlans = alsatianTestFileExecutionPlan.getTestSuiteExecutionPlans();
        AlsatianTestSuiteExecutionPlan alsatianTestSuiteExecutionPlan = testSuiteExecutionPlans.get(0);
        String suiteName = alsatianTestSuiteExecutionPlan.getSuiteName();
        this.cbTestSuite.setSelectedItem(suiteName);
        return alsatianTestSuiteExecutionPlan;
    }

    private void fillCbSuites(AlsatianFileStructure alsatianFileStructure) {
        this.cbTestSuite.removeAllItems();
        if (alsatianFileStructure != null) {
            List<AlsatianSuiteStructure> suites = alsatianFileStructure.getSuites();
            for (AlsatianSuiteStructure suite : suites) {
                this.cbTestSuite.addItem(suite.getName());
            }
        }
    }

    @NotNull
    private AlsatianTestFileExecutionPlan setTestFileFromConfig(AlsatianRunConfiguration runConfiguration) {
        AlsatianExecutionPlan alsatianExecutionPlan = runConfiguration.getRunSettings().getAlsatianExecutionPlan();
        AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = alsatianExecutionPlan.getTestFileExecutionPlans().get(0);
        this.txtTestFile.setText(alsatianTestFileExecutionPlan.getLocationUrl());
        return alsatianTestFileExecutionPlan;
    }

    public void applyEditorTo(AlsatianRunConfiguration runConfiguration) {
        AlsatianRunSettings.Builder builder = new AlsatianRunSettings.Builder();
        builder.setInterpreterRef(interpreterField.getInterpreterRef());
        builder.setEnvData(envVarComponent.getData());
        builder.setAlsatianPackage(alsatianPackageField.getSelected());
        AlsatianScopeKind scopeKind = this.getScopeKind();
        if (scopeKind != null) {
            builder.setScopeKind(scopeKind);
            builder.setWorkingDir(PathShortener.getAbsolutePath(this.txtWorkingDir.getTextField()));
            builder.setNodeOptions(this.txtNodeOptions.getText());
            AlsatianExecutionPlan alsatianExecutionPlan = new AlsatianExecutionPlan();
            if (scopeKind == AlsatianScopeKind.WILDCARD) {
                ArrayList<String> listOfGlobs = Collections.list(((DefaultListModel<String>) this.listOfGlobs.getModel()).elements());
                builder.setListOfGlobs(listOfGlobs);
                builder.setAlsatianExecutionPlan(alsatianExecutionPlan);
            } else if (scopeKind == AlsatianScopeKind.TEST_FILE) {
                String testFile = this.txtTestFile.getTextField().getText();
                alsatianExecutionPlan.addOrGetTestFile(testFile);
                builder.setAlsatianExecutionPlan(alsatianExecutionPlan);
            } else if (scopeKind == AlsatianScopeKind.SUITE) {
                String testFile = this.txtTestFile.getTextField().getText();
                AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = alsatianExecutionPlan.addOrGetTestFile(testFile);
                alsatianTestFileExecutionPlan.addOrGetSuite((String) this.cbTestSuite.getSelectedItem());
                builder.setAlsatianExecutionPlan(alsatianExecutionPlan);
            } else if (scopeKind == AlsatianScopeKind.TEST) {
                String testFile = this.txtTestFile.getTextField().getText();
                AlsatianTestFileExecutionPlan alsatianTestFileExecutionPlan = alsatianExecutionPlan.addOrGetTestFile(testFile);
                AlsatianTestSuiteExecutionPlan suite = alsatianTestFileExecutionPlan.addOrGetSuite((String) this.cbTestSuite.getSelectedItem());
                suite.addOrGetTest((String)this.cbTestMethod.getSelectedItem());
                builder.setAlsatianExecutionPlan(alsatianExecutionPlan);

            }

            runConfiguration.setRunSettings(builder.build());
        }
    }

    private AlsatianScopeKind getScopeKind() {
        if (rbAllTests.isSelected()) {
            return AlsatianScopeKind.WILDCARD;
        } else if (rbTestFile.isSelected()) {
            return AlsatianScopeKind.TEST_FILE;
        } else if (rbTest.isSelected()) {
            return AlsatianScopeKind.TEST;
        } else if (rbSuite.isSelected()) {
            return AlsatianScopeKind.SUITE;
        } else {
            return null;
        }
    }

    private void setScopeKind(AlsatianScopeKind scopeKind) {
        updateEditorVisibility(scopeKind);
        this.currentScopeKind = scopeKind;
        if (scopeKind == AlsatianScopeKind.WILDCARD) {
            this.rbAllTests.setSelected(true);
        } else if (scopeKind == AlsatianScopeKind.TEST_FILE) {
            this.rbTestFile.setSelected(true);
        } else if (scopeKind == AlsatianScopeKind.TEST) {
            this.rbTest.setSelected(true);
        } else if (scopeKind == AlsatianScopeKind.SUITE) {
            this.rbSuite.setSelected(true);
        } else {
            throw new IllegalStateException("unsupported scope kind" + scopeKind);
        }
    }

}
