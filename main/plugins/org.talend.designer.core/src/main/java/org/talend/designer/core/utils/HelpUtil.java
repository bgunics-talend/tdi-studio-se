// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.utils;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.PluginChecker;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

public class HelpUtil {

    private static final String JVM_PARAM_ONLINE_HELP_ENABLE = "online.help.enable"; //$NON-NLS-1$

    private static final String JVM_PARAM_ONLINE_HELP_VERSION = "online.help.version"; //$NON-NLS-1$

    private static final String BASE_HELP_ID_PREFIX = "org.talend.help";

    private static final String EXTERNAL_HELP_ID_PREFIX = "org.talend.help.external";

    private static String INTERNAL_VERSION = VersionUtils.getInternalVersion();

    private static boolean IS_RELEASE_VERSION = INTERNAL_VERSION.indexOf("-") < 0 //$NON-NLS-1$
            || INTERNAL_VERSION.toLowerCase().indexOf("patch") >= 0;

    private static String PRODUCT_BASE_VERSION = INTERNAL_VERSION.substring(0, 3);

    private static String HELP_LANGUAGE = Locale.FRENCH.equals(Locale.getDefault().getLanguage()) ? "fr" : "en"; //$NON-NLS-1$ //$NON-NLS-2$

    private static Boolean IS_HELP_INSTALLED = null;

    private static Boolean IS_BASE_HELP_INSTALLED = null;

    public static boolean isEnabledOnLineHelp() {
        return Boolean.parseBoolean(System.getProperty(JVM_PARAM_ONLINE_HELP_ENABLE, "false"));
    }

    public static boolean isUseOnLineHelp() {
        if (!isEnabledOnLineHelp()) {
            return false;
        }
        boolean isOffLineHelpInPre = DesignerPlugin.getDefault().getPreferenceStore()
                .getBoolean(TalendDesignerPrefConstants.HELP_OFFLINE);
        if (isHelpInstalled(true) && isOffLineHelpInPre) {
            return false;
        }
        return true;
    }

    public static String calOnLineHelpURL(String componentName) {
        StringBuilder sb = new StringBuilder();
        if (IS_RELEASE_VERSION) {
            sb.append("https://help.talend.com/access/sources/content/topic?pageid="); //$NON-NLS-1$
        } else {
            sb.append("https://talend-staging.fluidtopics.net/access/sources/content/topic?pageid="); //$NON-NLS-1$
        }
        sb.append(componentName.toLowerCase());
        sb.append("&afs:lang=").append(HELP_LANGUAGE); //$NON-NLS-1$
        sb.append("&EnrichVersion="); //$NON-NLS-1$
        if (!StringUtils.isEmpty(System.getProperty(JVM_PARAM_ONLINE_HELP_VERSION))) {
            sb.append(System.getProperty(JVM_PARAM_ONLINE_HELP_VERSION));
        } else {
            sb.append(PRODUCT_BASE_VERSION);
        }
        return sb.toString();
    }

    public static void openLineHelp(String componentName) {
        String url = calOnLineHelpURL(componentName);
        Program.launch(url);
    }

    public static boolean isHelpInstalled(boolean isUseCache) {
        if (IS_HELP_INSTALLED == null || !isUseCache) {
            IS_HELP_INSTALLED = true;
            if (!PluginChecker.isPluginLoaded(PluginChecker.HELP_DI_EE_PLUGIN_ID)) {
                IS_HELP_INSTALLED = false;
            }
            if (IS_HELP_INSTALLED && PluginChecker.isPluginLoaded(PluginChecker.ESBEE_PLUGIN_ID)
                    && !PluginChecker.isPluginLoaded(PluginChecker.HELP_ESB_PLUGIN_ID)) {
                IS_HELP_INSTALLED = false;
            }
            if (IS_HELP_INSTALLED && PluginChecker.isPluginLoaded(PluginChecker.BDEE_PLUGIN_ID)
                    && !PluginChecker.isPluginLoaded(PluginChecker.HELP_BD_PLUGIN_ID)) {
                IS_HELP_INSTALLED = false;
            }
        }
        return IS_HELP_INSTALLED;
    }

    public static void displayHelp(String helpId) {
        if (IS_BASE_HELP_INSTALLED == null) {
            IS_BASE_HELP_INSTALLED = PluginChecker.isPluginLoaded(PluginChecker.HELP_PLUGIN_ID);
        }

        if (!IS_BASE_HELP_INSTALLED && helpId.startsWith(BASE_HELP_ID_PREFIX)) {
            PlatformUI.getWorkbench().getHelpSystem()
                    .displayHelp(EXTERNAL_HELP_ID_PREFIX + helpId.substring(BASE_HELP_ID_PREFIX.length()));
            return;
        }
        PlatformUI.getWorkbench().getHelpSystem().displayHelp(helpId);
    }
}
