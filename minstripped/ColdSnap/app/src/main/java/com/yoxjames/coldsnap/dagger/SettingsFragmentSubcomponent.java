package com.yoxjames.coldsnap.dagger;

import com.yoxjames.coldsnap.ui.prefs.SettingsFragment;
import dagger.Subcomponent;

@Subcomponent(modules = { SettingsFragmentModule.class })
// TODO: Uhhh....
@ActivityScope
public interface SettingsFragmentSubcomponent {

    void inject(SettingsFragment settingsFragment);
}
