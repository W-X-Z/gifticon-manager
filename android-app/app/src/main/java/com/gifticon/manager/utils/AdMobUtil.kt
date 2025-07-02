package com.gifticon.manager.utils

import com.gifticon.manager.BuildConfig
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdListener

object AdMobUtil {
    
    // 테스트 광고 ID들
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    
    // 실제 광고 ID들
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-3722799840497343/1165750116"
    const val PROD_APP_ID = "ca-app-pub-3722799840497343~2515356774"
    
    /**
     * 현재 사용할 광고 ID를 반환합니다.
     * 개발 중에는 테스트 ID를, 배포 시에는 실제 ID를 사용합니다.
     */
    fun getBannerAdUnitId(): String {
        return if (BuildConfig.DEBUG) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
    }
    
    /**
     * 현재 사용할 앱 ID를 반환합니다.
     */
    fun getAppId(): String {
        return if (BuildConfig.DEBUG) TEST_APP_ID else PROD_APP_ID
    }
    
    /**
     * 광고 로드 에러 처리를 위한 기본 리스너
     */
    fun createAdListener(
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (LoadAdError) -> Unit = {}
    ): AdListener {
        return object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                onAdLoaded()
            }
            
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                onAdFailedToLoad(loadAdError)
            }
        }
    }
    
    /**
     * 기본 AdRequest를 생성합니다.
     */
    fun createAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
} 