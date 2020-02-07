/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tpsadvertising.orientnews.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;


/**
 * Helper for working with launcher shortcuts.
 */
public class ShortcutHelper {

    private static final String SEARCH_SHORTCUT_ID = "search";
    private static final String POST_SHORTCUT_ID = "post_dn_story";
    private static final List<String> DYNAMIC_SHORTCUT_IDS
            = Collections.singletonList(POST_SHORTCUT_ID);

    private ShortcutHelper() { }



    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static void disablePostShortcut(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return;
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        shortcutManager.disableShortcuts(DYNAMIC_SHORTCUT_IDS);
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static void reportPostUsed(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return;
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        shortcutManager.reportShortcutUsed(POST_SHORTCUT_ID);
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static void reportSearchUsed(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return;
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        shortcutManager.reportShortcutUsed(SEARCH_SHORTCUT_ID);
    }

}
