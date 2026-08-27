package org.openforis.collect.android.gui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.openforis.collect.Collect;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.App;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription(getString(R.string.main_title))
                .setImage(R.drawable.ic_logo)
                .addItem(new Element().setTitle(String.format("Version: %s - Collect Core version: %s",
                        App.versionFull(this),
                        Collect.VERSION.toString()
                )))
                .addWebsite("https://github.com/openforis/collect-mobile/blob/master/CHANGELOG.md", "Version Changelog")
                .addWebsite("http://www.openforis.org/tools/collect-mobile")
                .addGroup("Contact us")
                .addWebsite("http://www.openforis.org/support", "Open Foris Support forum")
                .addTwitter("openforis")
                .addPlayStore("org.openforis.collect")
                .addGitHub("openforis/collect-mobile")
                .create();

        setContentView(aboutPage);

        // the page background is always white, regardless of the app's dark/light theme setting,
        // so the status bar icons must be dark to stay visible against it (edge-to-edge content
        // draws behind the now-transparent status bar on Android 15+)
        new WindowInsetsControllerCompat(getWindow(), aboutPage).setAppearanceLightStatusBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(aboutPage, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

}
