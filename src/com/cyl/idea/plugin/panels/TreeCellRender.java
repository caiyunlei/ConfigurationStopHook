package com.cyl.idea.plugin.panels;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.impl.SingleConfigurationConfigurable;
import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.EmptyIcon;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jetbrains.annotations.NotNull;

/**
 * todo:直接使用kt文件
 * copy from com.intellij.execution.impl.RunConfigurableTreeRenderer
 */
public class TreeCellRender extends ColoredTreeCellRenderer {
    private RunManagerImpl myRunManager;

    public TreeCellRender(RunManagerImpl runManager) {
        myRunManager = runManager;
    }

    @Override
    public void customizeCellRenderer(@NotNull JTree jTree, Object o, boolean b, boolean b1, boolean b2, int i, boolean b3) {
        if (!(o instanceof DefaultMutableTreeNode)) {
            return;
        }

        DefaultMutableTreeNode value = (DefaultMutableTreeNode) o;

        Object userObject = value.getUserObject();
        Boolean isShared = null;
        String name = getUserObjectName(userObject);
        if (userObject instanceof ConfigurationType) {
            SimpleTextAttributes simpleTextAttributes;
            if (((DefaultMutableTreeNode) value.getParent()).isRoot()) {
                simpleTextAttributes = SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
            } else {
                simpleTextAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
            }
            append(name, simpleTextAttributes);
            setIcon(((ConfigurationType) userObject).getIcon());
        } else if (userObject instanceof String) {
            append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setIcon(AllIcons.Nodes.Folder);
        } else if (userObject instanceof ConfigurationFactory) {
            append(name);
            setIcon(((ConfigurationFactory) userObject).getIcon());
        } else {
            RunnerAndConfigurationSettings settings = null;
            if (userObject instanceof SingleConfigurationConfigurable) {
                settings = (RunnerAndConfigurationSettings) ((SingleConfigurationConfigurable) userObject).getSettings();
                isShared = ((SingleConfigurationConfigurable) userObject).isStoreProjectConfiguration();
                setIcon(ProgramRunnerUtil.getConfigurationIcon(settings, !((SingleConfigurationConfigurable) userObject).isValid()));
            } else if (userObject instanceof RunnerAndConfigurationSettingsImpl) {
                isShared = ((RunnerAndConfigurationSettingsImpl) userObject).isShared();
                setIcon(myRunManager.getConfigurationIcon((RunnerAndConfigurationSettings) userObject));
                settings = (RunnerAndConfigurationSettings) userObject;
            }

            if (settings != null) {
                SimpleTextAttributes simpleTextAttributes;
                if (settings.isTemporary()) {
                    simpleTextAttributes = SimpleTextAttributes.GRAY_ATTRIBUTES;
                } else {
                    simpleTextAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
                }
                append(name, simpleTextAttributes);
            }
        }

        if (isShared == null) {
            setIconTextGap(2);
        } else {
            Icon icon;
            if (isShared) {
                icon = AllIcons.Nodes.Shared;
            } else {
                icon = EmptyIcon.ICON_16;
            }
            LayeredIcon layeredIcon = new LayeredIcon(getIcon(), icon);
            setIcon(layeredIcon);
            setIconTextGap(0);
        }
    }

    private String getUserObjectName(Object userObject) {
        if (userObject instanceof ConfigurationType) {
            return ((ConfigurationType) userObject).getDisplayName();
        } else if (userObject instanceof ConfigurationFactory) {
            return ((ConfigurationFactory) userObject).getName();
        } else if (userObject instanceof SingleConfigurationConfigurable) {
            return ((SingleConfigurationConfigurable) userObject).getNameText();
        } else if (userObject instanceof RunnerAndConfigurationSettingsImpl) {
            return ((RunnerAndConfigurationSettingsImpl) userObject).getName();
        } else {
            return userObject.toString();
        }
    }
}
