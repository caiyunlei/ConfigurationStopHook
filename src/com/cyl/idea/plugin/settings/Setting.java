package com.cyl.idea.plugin.settings;

import com.cyl.idea.plugin.panels.TasksBeforeStopApplicationPanel;
import com.cyl.idea.plugin.panels.TreeCellRender;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class Setting implements Configurable {
    private JPanel myGeneralPanel;
    private Tree tree;
    private JPanel myRightPanel;
    private PropertiesComponent myPropertiesComponent;

    public Setting(PropertiesComponent propertiesComponent) {
        myGeneralPanel = new JPanel();
        myGeneralPanel.setLayout(new BorderLayout());

        myRightPanel = new JPanel(new BorderLayout());
        myRightPanel.add(new TasksBeforeStopApplicationPanel(), BorderLayout.CENTER);
        tree = new Tree();
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new TreeCellRender(getRunManager()));
        JSplitPane comp = new JSplitPane(SwingConstants.VERTICAL, tree, myRightPanel);
        comp.setDividerLocation(250);

        myGeneralPanel.add(comp);
        myPropertiesComponent = propertiesComponent;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "TasksBeforeStopApplication";
    }

    @Override
    public JComponent createComponent() {
        initTree();
        return myGeneralPanel;
    }

    private void initTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        TreeModel treeModel = new DefaultTreeModel(root);
        tree.setModel(treeModel);
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
        tree.expandRow(0);
        tree.setRootVisible(false);
    }

    @NotNull
    private RunManagerImpl getRunManager() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        //todo:multiple projects
        return (RunManagerImpl) RunManagerImpl.getInstance(projects[0]);
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
