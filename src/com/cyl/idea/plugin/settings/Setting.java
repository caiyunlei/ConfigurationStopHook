package com.cyl.idea.plugin.settings;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.util.List;
import java.util.Map;

public class Setting implements Configurable {
    private JPanel myGeneralPanel;
    private JTree tree1;
    private JPanel myRightPanel;
    private PropertiesComponent myPropertiesComponent;

    public Setting(PropertiesComponent propertiesComponent) {
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
        tree1.setModel(treeModel);
//        tree1.setRootVisible(false);
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        //todo:multiple projects
        RunManagerImpl runManager = (RunManagerImpl) RunManagerImpl.getInstance(projects[0]);
        Map<ConfigurationType, Map<String, List<RunnerAndConfigurationSettings>>> runConfigurations =
                runManager.getConfigurationsGroupedByTypeAndFolder(true);

        for (Map.Entry<ConfigurationType, Map<String, List<RunnerAndConfigurationSettings>>> configurationTypeMapEntry :
                runConfigurations.entrySet()) {
            ConfigurationType type = configurationTypeMapEntry.getKey();
            Map<String, List<RunnerAndConfigurationSettings>> folderMap = configurationTypeMapEntry.getValue();

            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(type);
            root.add(typeNode);
            for (Map.Entry<String,List<RunnerAndConfigurationSettings>> entry : folderMap.entrySet()) {
                DefaultMutableTreeNode node = null;
                String folder = entry.getKey();
                if (folder == null) {
                    node = typeNode;
                }
                else {
                    node = new DefaultMutableTreeNode(folder);
                    typeNode.add(node);
                }

                for (RunnerAndConfigurationSettings settings: entry.getValue()) {
                    node.add(new DefaultMutableTreeNode(settings));
                }
            }
        }
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
