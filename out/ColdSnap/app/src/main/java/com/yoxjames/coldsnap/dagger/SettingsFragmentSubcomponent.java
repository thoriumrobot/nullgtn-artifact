package com.yoxjames.coldsnap.dagger;

import com.yoxjames.coldsnap.ui.prefs.SettingsFragment;
import dagger.Subcomponent;
import javax.annotation.Nullable;

@Subcomponent(modules = { SettingsFragmentModule.class })
// TODO: Uhhh....
@ActivityScope
public interface SettingsFragmentSubcomponent {

    void inject(@Nullable() SettingsFragment settingsFragment);
}
