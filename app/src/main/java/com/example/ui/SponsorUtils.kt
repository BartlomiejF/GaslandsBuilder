package com.example.ui

import com.example.data.models.GaslandsSponsor

fun getSponsoredPerks(sponsor: GaslandsSponsor): List<String> {
    val desc = sponsor.description
    val key = "Sponsored Perks:"
    val index = desc.indexOf(key)
    if (index == -1) return emptyList()
    val perksPart = desc.substring(index + key.length).trim()
    return perksPart.split(Regex("\\.\\s*")).map { it.trim() }.filter { it.isNotEmpty() }
}
