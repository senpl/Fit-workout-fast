package com.easyfitness.intro

import android.os.Bundle
import com.easyfitness.DAO.DAOProfil
import com.easyfitness.R
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import com.heinrichreimersoftware.materialintro.slide.Slide

class MainIntroActivity : IntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = intent

        val EXTRA_SHOW_BACK = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SHOW_BACK"
        val EXTRA_SHOW_NEXT = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SHOW_NEXT"
        val EXTRA_SKIP_ENABLED = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SKIP_ENABLED"
        val EXTRA_FINISH_ENABLED = "com.heinrichreimersoftware.materialintro.demo.EXTRA_FINISH_ENABLED"
        val EXTRA_GET_STARTED_ENABLED = "com.heinrichreimersoftware.materialintro.demo.EXTRA_GET_STARTED_ENABLED"
        val showBack = intent.getBooleanExtra(EXTRA_SHOW_BACK, true)
        val showNext = intent.getBooleanExtra(EXTRA_SHOW_NEXT, true)
        val skipEnabled = intent.getBooleanExtra(EXTRA_SKIP_ENABLED, false)
        val finishEnabled = intent.getBooleanExtra(EXTRA_FINISH_ENABLED, true)
        val getStartedEnabled = intent.getBooleanExtra(EXTRA_GET_STARTED_ENABLED, false)
        isFullscreen = false
        super.onCreate(savedInstanceState)
        buttonBackFunction = if (skipEnabled) BUTTON_BACK_FUNCTION_SKIP else BUTTON_BACK_FUNCTION_BACK
        buttonNextFunction = if (finishEnabled) BUTTON_NEXT_FUNCTION_NEXT_FINISH else BUTTON_NEXT_FUNCTION_NEXT
        isButtonBackVisible = showBack
        isButtonNextVisible = showNext
        isButtonCtaVisible = getStartedEnabled
        buttonCtaTintMode = BUTTON_CTA_TINT_MODE_TEXT
        addSlide(SimpleSlide.Builder()
            .title(R.string.introSlide1Title)
            .description(R.string.introSlide1Text)
            .image(R.drawable.iconman)
            .background(R.color.launcher_background)
            .backgroundDark(R.color.launcher_background)
            .scrollable(true)
            .build())
        addSlide(SimpleSlide.Builder()
            .title(R.string.introSlide2Title)
            .description(R.string.introSlide2Text)
            .image(R.drawable.bench_hi_res_512)
            .background(R.color.launcher_background)
            .backgroundDark(R.color.launcher_background)
            .scrollable(true)
            .build())
        addSlide(SimpleSlide.Builder()
            .title(R.string.titleSlideEssential)
            .description(R.string.textSlideEssential)
            .image(R.drawable.idea_hi_res_485)
            .background(R.color.launcher_background)
            .backgroundDark(R.color.launcher_background)
            .scrollable(true)
            .build())

//        addSlide(new SimpleSlide.Builder()
//            .title(R.string.titleSlideOpenSource)
//            .description(R.string.textSlideOpenSource)
//            .image(R.drawable.group_hi_res_512)
//            .background(R.color.launcher_background)
//            .backgroundDark(R.color.launcher_background)
//            .scrollable(true)
//            .build());

/*
        final Slide permissionsSlide;
        if (permissions) {
            permissionsSlide = new SimpleSlide.Builder()
                .title(R.string.introSlide3Title)
                .description(R.string.introSlide3Text)
                .image(R.drawable.ic_settings_black_48dp)
                .background(R.color.tableheader_background)
                .backgroundDark(R.color.background_odd)
                .scrollable(true)
                .permissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .build();
            addSlide(permissionsSlide);
        } else {
            permissionsSlide = null;
        }
*/

        // Initialisation des objets DB
        val mDbProfils = DAOProfil(this.applicationContext)

        // Pour la base de donnee profil, il faut toujours qu'il y ai au moins un profil
        if (mDbProfils.count == 0) {
            val profileSlide: Slide
            // Ouvre la fenetre de creation de profil
            profileSlide = FragmentSlide.Builder()
                .background(R.color.launcher_background)
                .backgroundDark(R.color.launcher_background)
                .fragment(NewProfileFragment.newInstance())
                .build()
            addSlide(profileSlide)
        }
    }
}
