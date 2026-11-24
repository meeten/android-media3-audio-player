package com.example.mediastoreexampleapp.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

val ColorScheme.inactiveTrackColor: Color
    get() = if (this.background == Black900) Color(0xFF403C50) else Color(0xFFE2E5EB)

val ColorScheme.inactiveTrackColorForMiniPlayer: Color
    get() = if (this.background == Black900) Color(0xFF403C50) else Color(0xFFDBD6fC)