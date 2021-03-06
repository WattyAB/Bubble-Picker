package com.igalata.bubblepicker.model

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt

/**
 * Created by irinagalata on 1/19/17.
 */
data class PickerItem @JvmOverloads constructor(val id: String,
                                                var title: String? = null,
                                                var icon: Drawable? = null,
                                                var iconOnTop: Boolean = true,
                                                @ColorInt var color: Int? = null,
                                                var gradient: BubbleGradient? = null,
                                                var overlayAlpha: Float = 0.5f,
                                                var type: Int? = null,
                                                var typeface: Typeface = Typeface.DEFAULT,
                                                @ColorInt val textColor: Int? = null,
                                                var textSize: Float = 40f,
                                                var backgroundImage: Drawable? = null,
                                                var isSelected: Boolean = false)
