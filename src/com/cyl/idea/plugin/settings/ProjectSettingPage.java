package com.cyl.idea.plugin.settings;

import com.cyl.idea.plugin.MyProjectUtil;
import com.cyl.idea.plugin.panels.BeforeTerminalTasksPanel;
import com.cyl.idea.plugin.panels.TreeCellRender;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ProjectSettingPage implements Configurable {
    private JPanel myWholePanel;
    private Tree myConfigurationTree;
    private JPanel myRightPanel;

    public ProjectSettingPage(PropertiesComponent propertiesComponent) {
        myWholePanel = new JPanel();
        myWholePanel.setLayout(new BorderLayout());

        myConfigurationTree = new Tree();
        myConfigurationTree.setShowsRootHandles(true);
        myConfigurationTree.setCellRenderer(new TreeCellRender(getRunManager()));

        myRightPanel = new JPanel(new BorderLayout());

        JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, myConfigurationTree,
            myRightPanel);
        splitPanel.setDividerLocation(250);

        myWholePanel.add(splitPanel);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "TasksBeforeStopApplication";
    }

    @Override
    public JComponent createComponent() {
        initTree();
        return myWholePanel;
    }

    private void initTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        myConfigurationTree.setModel(new DefaultTreeModel(root));

        buildTreeNode(root);

        myConfigurationTree.expandRow(0);
        myConfigurationTree.setRootVisible(false);
        TreeUtil.expandAll(myConfigurationTree);

        myConfigurationTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            //com/intellij/execution/impl/RunConfigurable.kt
            TreePath selectionPath = myConfigurationTree.getSelectionPath();
            if (selectionPath != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                getSafeUserObject(node);
            }
        });
    }

    private void buildTreeNode(DefaultMutableTreeNode root) {
        //runConfigurable data come from here
        RunManagerImpl runManager = getRunManager();
        Map<ConfigurationType, Map<String, List<RunnerAndConfigurationSettings>>> runConfigurations =
                runManager.getConfigurationsGroupedByTypeAndFolder(true);

        for (Map.Entry<ConfigurationType, Map<String, List<RunnerAndConfigurationSettings>>> configurationTypeMapEntry :
                runConfigurations.entrySet()) {
            ConfigurationType type = configurationTypeMapEntry.getKey();
            Map<String, List<RunnerAndConfigurationSettings>> folderMap = configurationTypeMapEntry.getValue();

            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(type);
            root.add(typeNode);
            for (Map.Entry<String, List<RunnerAndConfigurationSettings>> entry : folderMap.entrySet()) {
                DefaultMutableTreeNode node = null;
                String folder = entry.getKey();
                if (folder == null) {
                    node = typeNode;
                } else {
                    node = new DefaultMutableTreeNode(folder);
                    typeNode.add(node);
                }

                for (RunnerAndConfigurationSettings settings : entry.getValue()) {
                    node.add(new DefaultMutableTreeNode(settings));
                }
            }
        }
    }

    @NotNull
    private RunManagerImpl getRunManager() {
        return (RunManagerImpl) RunManagerImpl.getInstance(MyProjectUtil.getCurrentProject());
    }

    @Override
    public boolean isModified() {


        return true;
    }

    @Override
    public void apply() {
    }


    @Override
    public void reset() {
    }

    private Object getSafeUserObject(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof RunnerAndConfigurationSettingsImpl) {
            BeforeTerminalTasksPanel newRightPanel =
                new BeforeTerminalTasksPanel((RunnerAndConfigurationSettings) userObject);
            updateRightPanel(newRightPanel);
        }
        return userObject;
    }

    private void updateRightPanel(BeforeTerminalTasksPanel newRightPanel) {
        myRightPanel.removeAll();
        myRightPanel.add(newRightPanel, BorderLayout.CENTER);
    }
}
