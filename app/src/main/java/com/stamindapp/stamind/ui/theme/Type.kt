package com.stamindapp.stamind.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.stamindapp.stamind.R

// ==================== NUNITO FONT FAMILIES ====================
val NunitoRegularFamily = FontFamily(Font(R.font.nunito_regular))
val NunitoMediumFamily = FontFamily(Font(R.font.nunito_medium))
val NunitoSemiBoldFamily = FontFamily(Font(R.font.nunito_semibold))
val NunitoBoldFamily = FontFamily(Font(R.font.nunito_bold))
val NunitoExtraBoldFamily = FontFamily(Font(R.font.nunito_extrabold))
val NunitoBoldItalicFamily = FontFamily(Font(R.font.nunito_bolditalic))

// ==================== LEXEND FONT FAMILIES ====================
val LexendRegularFamily = FontFamily(Font(R.font.lexend_regular))
val LexendMediumFamily = FontFamily(Font(R.font.lexend_medium))
val LexendSemiBoldFamily = FontFamily(Font(R.font.lexend_semibold))
val LexendBoldFamily = FontFamily(Font(R.font.lexend_bold))
val LexendExtraBoldFamily = FontFamily(Font(R.font.lexend_extrabold))


private fun createTextStyle(fontFamily: FontFamily, fontSize: Int) = TextStyle(
    fontFamily = fontFamily,
    fontSize = fontSize.sp,
    lineHeight = (fontSize * 1.4).sp
)


object NunitoTypography {
    // REGULAR TYPE
    val Regular1 = createTextStyle(NunitoRegularFamily, 48)
    val Regular2 = createTextStyle(NunitoRegularFamily, 40)
    val Regular3 = createTextStyle(NunitoRegularFamily, 33)
    val Regular4 = createTextStyle(NunitoRegularFamily, 28)
    val Regular5 = createTextStyle(NunitoRegularFamily, 23)
    val Regular6 = createTextStyle(NunitoRegularFamily, 19)
    val Regular7 = createTextStyle(NunitoRegularFamily, 16)
    val Regular8 = createTextStyle(NunitoRegularFamily, 13)
    val Regular9 = createTextStyle(NunitoRegularFamily, 11)

    // MEDIUM TYPE
    val Medium1 = createTextStyle(NunitoMediumFamily, 48)
    val Medium2 = createTextStyle(NunitoMediumFamily, 40)
    val Medium3 = createTextStyle(NunitoMediumFamily, 33)
    val Medium4 = createTextStyle(NunitoMediumFamily, 28)
    val Medium5 = createTextStyle(NunitoMediumFamily, 23)
    val Medium6 = createTextStyle(NunitoMediumFamily, 19)
    val Medium7 = createTextStyle(NunitoMediumFamily, 16)
    val Medium8 = createTextStyle(NunitoMediumFamily, 13)
    val Medium9 = createTextStyle(NunitoMediumFamily, 11)

    // SEMIBOLD TYPE
    val SemiBold1 = createTextStyle(NunitoSemiBoldFamily, 48)
    val SemiBold2 = createTextStyle(NunitoSemiBoldFamily, 40)
    val SemiBold3 = createTextStyle(NunitoSemiBoldFamily, 33)
    val SemiBold4 = createTextStyle(NunitoSemiBoldFamily, 28)
    val SemiBold5 = createTextStyle(NunitoSemiBoldFamily, 23)
    val SemiBold6 = createTextStyle(NunitoSemiBoldFamily, 19)
    val SemiBold7 = createTextStyle(NunitoSemiBoldFamily, 16)
    val SemiBold8 = createTextStyle(NunitoSemiBoldFamily, 13)
    val SemiBold9 = createTextStyle(NunitoSemiBoldFamily, 11)

    // BOLD TYPE
    val Bold1 = createTextStyle(NunitoBoldFamily, 48)
    val Bold2 = createTextStyle(NunitoBoldFamily, 40)
    val Bold3 = createTextStyle(NunitoBoldFamily, 33)
    val Bold4 = createTextStyle(NunitoBoldFamily, 28)
    val Bold5 = createTextStyle(NunitoBoldFamily, 23)
    val Bold6 = createTextStyle(NunitoBoldFamily, 19)
    val Bold7 = createTextStyle(NunitoBoldFamily, 16)
    val Bold8 = createTextStyle(NunitoBoldFamily, 13)
    val Bold9 = createTextStyle(NunitoBoldFamily, 11)

    // EXTRABOLD
    val ExtraBold1 = createTextStyle(NunitoExtraBoldFamily, 48)
    val ExtraBold2 = createTextStyle(NunitoExtraBoldFamily, 40)
    val ExtraBold3 = createTextStyle(NunitoExtraBoldFamily, 33)
    val ExtraBold4 = createTextStyle(NunitoExtraBoldFamily, 28)
    val ExtraBold5 = createTextStyle(NunitoExtraBoldFamily, 23)
    val ExtraBold6 = createTextStyle(NunitoExtraBoldFamily, 19)
    val ExtraBold7 = createTextStyle(NunitoExtraBoldFamily, 16)
    val ExtraBold8 = createTextStyle(NunitoExtraBoldFamily, 13)
    val ExtraBold9 = createTextStyle(NunitoExtraBoldFamily, 11)

    // BOLD ITALIC TYPE
    val BoldItalic1 = createTextStyle(NunitoBoldItalicFamily, 48)
    val BoldItalic2 = createTextStyle(NunitoBoldItalicFamily, 40)
    val BoldItalic3 = createTextStyle(NunitoBoldItalicFamily, 33)
    val BoldItalic4 = createTextStyle(NunitoBoldItalicFamily, 28)
    val BoldItalic5 = createTextStyle(NunitoBoldItalicFamily, 23)
    val BoldItalic6 = createTextStyle(NunitoBoldItalicFamily, 19)
    val BoldItalic7 = createTextStyle(NunitoBoldItalicFamily, 16)
    val BoldItalic8 = createTextStyle(NunitoBoldItalicFamily, 13)
    val BoldItalic9 = createTextStyle(NunitoBoldItalicFamily, 11)
}

object LexendTypography {
    // REGULAR TYPE
    val Regular1 = createTextStyle(LexendRegularFamily, 48)
    val Regular2 = createTextStyle(LexendRegularFamily, 40)
    val Regular3 = createTextStyle(LexendRegularFamily, 33)
    val Regular4 = createTextStyle(LexendRegularFamily, 28)
    val Regular5 = createTextStyle(LexendRegularFamily, 23)
    val Regular6 = createTextStyle(LexendRegularFamily, 19)
    val Regular7 = createTextStyle(LexendRegularFamily, 16)
    val Regular8 = createTextStyle(LexendRegularFamily, 13)
    val Regular9 = createTextStyle(LexendRegularFamily, 11)

    // MEDIUM TYPE
    val Medium1 = createTextStyle(LexendMediumFamily, 48)
    val Medium2 = createTextStyle(LexendMediumFamily, 40)
    val Medium3 = createTextStyle(LexendMediumFamily, 33)
    val Medium4 = createTextStyle(LexendMediumFamily, 28)
    val Medium5 = createTextStyle(LexendMediumFamily, 23)
    val Medium6 = createTextStyle(LexendMediumFamily, 19)
    val Medium7 = createTextStyle(LexendMediumFamily, 16)
    val Medium8 = createTextStyle(LexendMediumFamily, 13)
    val Medium9 = createTextStyle(LexendMediumFamily, 11)

    // SEMIBOLD TYPE
    val SemiBold1 = createTextStyle(LexendSemiBoldFamily, 48)
    val SemiBold2 = createTextStyle(LexendSemiBoldFamily, 40)
    val SemiBold3 = createTextStyle(LexendSemiBoldFamily, 33)
    val SemiBold4 = createTextStyle(LexendSemiBoldFamily, 28)
    val SemiBold5 = createTextStyle(LexendSemiBoldFamily, 23)
    val SemiBold6 = createTextStyle(LexendSemiBoldFamily, 19)
    val SemiBold7 = createTextStyle(LexendSemiBoldFamily, 16)
    val SemiBold8 = createTextStyle(LexendSemiBoldFamily, 13)
    val SemiBold9 = createTextStyle(LexendSemiBoldFamily, 11)

    // BOLD TYPE
    val Bold1 = createTextStyle(LexendBoldFamily, 48)
    val Bold2 = createTextStyle(LexendBoldFamily, 40)
    val Bold3 = createTextStyle(LexendBoldFamily, 33)
    val Bold4 = createTextStyle(LexendBoldFamily, 28)
    val Bold5 = createTextStyle(LexendBoldFamily, 23)
    val Bold6 = createTextStyle(LexendBoldFamily, 19)
    val Bold7 = createTextStyle(LexendBoldFamily, 16)
    val Bold8 = createTextStyle(LexendBoldFamily, 13)
    val Bold9 = createTextStyle(LexendBoldFamily, 11)

    // EXTRABOLD TYPE
    val ExtraBold1 = createTextStyle(LexendExtraBoldFamily, 48)
    val ExtraBold2 = createTextStyle(LexendExtraBoldFamily, 40)
    val ExtraBold3 = createTextStyle(LexendExtraBoldFamily, 33)
    val ExtraBold4 = createTextStyle(LexendExtraBoldFamily, 28)
    val ExtraBold5 = createTextStyle(LexendExtraBoldFamily, 23)
    val ExtraBold6 = createTextStyle(LexendExtraBoldFamily, 19)
    val ExtraBold7 = createTextStyle(LexendExtraBoldFamily, 16)
    val ExtraBold8 = createTextStyle(LexendExtraBoldFamily, 13)
    val ExtraBold9 = createTextStyle(LexendExtraBoldFamily, 11)
}
