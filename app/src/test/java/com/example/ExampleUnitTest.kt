package com.example

import org.junit.Test
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.File
import java.net.URL
import java.sql.DriverManager

class ExampleUnitTest {
  @Test
  fun downloadAndInspectDatabase() {
    println("--- START FILE GENERATING ---")
    val dbFile = File("gaslandsWeapons.sqlite3")
    try {
      val url = URL("https://github.com/BartlomiejF/GaslandsBuilder/raw/master/app/src/main/assets/databases/gaslandsWeapons.sqlite3")
      val bis = BufferedInputStream(url.openStream())
      val fos = FileOutputStream(dbFile)
      val buffer = ByteArray(4096)
      var count: Int
      while (bis.read(buffer, 0, 4096).also { count = it } != -1) {
        fos.write(buffer, 0, count)
      }
      fos.close()
      bis.close()

      Class.forName("org.sqlite.JDBC")
      val conn = DriverManager.getConnection("jdbc:sqlite:gaslandsWeapons.sqlite3")
      
      val sb = StringBuilder()
      sb.append("package com.example.data.models\n\n")
      sb.append("object GaslandsStaticData {\n")

      // 1. VEHICLES (Chassis) - GaslandsChassis(id, name, cost, weight, maxHull, handling, maxGear, crew, buildSlots, specialRules)
      sb.append("    val chassisList = listOf(\n")
      val vehStmt = conn.createStatement()
      val vehRs = vehStmt.executeQuery("SELECT * FROM vehicles ORDER BY id")
      while (vehRs.next()) {
        val idStr = vehRs.getString("name").lowercase().replace(" ", "_").replace("-", "_")
        val name = vehRs.getString("name")
        val cost = vehRs.getInt("cost")
        val buildSlots = vehRs.getInt("buildSlots")
        val maxGear = vehRs.getInt("maxGear")
        val handling = vehRs.getInt("handling")
        val maxHull = vehRs.getInt("hull")
        val crew = vehRs.getInt("crew")
        val specialRules = (vehRs.getString("specialRules") ?: "").replace("\"", "\\\"")
        val weight = vehRs.getString("weight") ?: ""
        sb.append("        GaslandsChassis(\"$idStr\", \"$name\", $cost, \"$weight\", $maxHull, $handling, $maxGear, $crew, $buildSlots, \"$specialRules\"),\n")
      }
      vehRs.close()
      vehStmt.close()
      sb.append("    )\n\n")

      // 2. SPONSORS - GaslandsSponsor(id, name, perkClasses, description, styleColorHex)
      sb.append("    val sponsorList = listOf(\n")
      sb.append("        GaslandsSponsor(\"unaligned\", \"Unaligned / None\", emptyList(), \"No specific sponsor, generic build rules apply.\", \"#90A4AE\"),\n")
      val spStmt = conn.createStatement()
      val spRs = spStmt.executeQuery("SELECT * FROM sponsors ORDER BY id")
      while (spRs.next()) {
        val name = spRs.getString("name")
        val idStr = name.lowercase().replace(" ", "_").replace("-", "_").replace("'", "")
        val perkI = spRs.getString("perkClassI") ?: ""
        val perkII = spRs.getString("perkClassII") ?: ""
        val sponsoredPerks = (spRs.getString("sponsoredPerks") ?: "").replace("\"", "\\\"")
        
        val colorHex = when (idStr) {
          "rutherford" -> "#B71C1C"
          "miyazaki" -> "#D81B60"
          "mishkin" -> "#00838F"
          "idris" -> "#E65100"
          "slime" -> "#4CAF50"
          "verney" -> "#F57F17"
          "beverly" -> "#7E57C2"
          "highway_patrol" -> "#0D47A1"
          "maxxene" -> "#0288D1"
          "the_warden" -> "#455A64"
          "order_of_the_inferno" -> "#E64A19"
          "rustys_bootleggers" -> "#5D4037"
          "scarletts_scavengers" -> "#FBC02D"
          else -> "#90A4AE"
        }
        val desc = "Perk Classes: $perkI, $perkII. Sponsored Perks: $sponsoredPerks"
        sb.append("        GaslandsSponsor(\"$idStr\", \"$name\", listOf(\"$perkI\", \"$perkII\"), \"$desc\", \"$colorHex\"),\n")
      }
      spRs.close()
      spStmt.close()
      sb.append("    )\n\n")

      // 3. WEAPONS - GaslandsWeapon(id, name, cost, slots, range, damage, maxAmmo, specialRules)
      sb.append("    val weaponList = listOf(\n")
      val wStmt = conn.createStatement()
      val wRs = wStmt.executeQuery("SELECT * FROM weapons ORDER BY id")
      while (wRs.next()) {
        val name = wRs.getString("name")
        val idStr = name.lowercase().replace(" ", "_").replace("-", "_").replace("/", "_").replace(".", "_")
        val cost = wRs.getInt("cost")
        val slots = wRs.getInt("buildSlots")
        val maxAmmo = wRs.getInt("ammo")
        val specialRules = (wRs.getString("specialRules") ?: "").replace("\"", "\\\"")
        val crewFired = wRs.getInt("crewFired") == 1
        val damage = wRs.getString("damage") ?: ""
        val range = wRs.getString("range") ?: ""
        val fullSpecial = if (crewFired) {
          if (specialRules.isNotEmpty()) "Crew-fired. $specialRules" else "Crew-fired"
        } else {
          specialRules
        }
        sb.append("        GaslandsWeapon(\"$idStr\", \"$name\", $cost, $slots, \"$range\", \"$damage\", $maxAmmo, \"$fullSpecial\"),\n")
      }
      wRs.close()
      wStmt.close()
      sb.append("    )\n\n")

      // 4. UPGRADES - GaslandsUpgrade(id, name, cost, slots, description)
      sb.append("    val upgradeList = listOf(\n")
      val uStmt = conn.createStatement()
      val uRs = uStmt.executeQuery("SELECT * FROM upgrades ORDER BY id")
      while (uRs.next()) {
        val name = uRs.getString("name")
        val idStr = name.lowercase().replace(" ", "_").replace("-", "_").replace("'", "")
        val cost = uRs.getInt("cost")
        val slots = uRs.getInt("buildSlots")
        val ammo = uRs.getInt("ammo")
        val specRules = (uRs.getString("specRules") ?: "").replace("\"", "\\\"")
        val fullDesc = if (ammo > 0) "$specRules (Ammo: $ammo)" else specRules
        sb.append("        GaslandsUpgrade(\"$idStr\", \"$name\", $cost, $slots, \"$fullDesc\"),\n")
      }
      uRs.close()
      uStmt.close()
      sb.append("    )\n\n")

      // 5. PERKS - GaslandsPerk(id, name, perkClass, cost)
      sb.append("    val perkList = listOf(\n")
      val pStmt = conn.createStatement()
      val pRs = pStmt.executeQuery("SELECT * FROM perks ORDER BY id")
      while (pRs.next()) {
        val name = pRs.getString("name")
        val idStr = name.lowercase().replace(" ", "_").replace("-", "_").replace("'", "")
        val clazz = pRs.getString("class")
        val cost = pRs.getInt("cost")
        sb.append("        GaslandsPerk(\"$idStr\", \"$name\", \"$clazz\", $cost),\n")
      }
      pRs.close()
      pStmt.close()
      sb.append("    )\n")

      sb.append("}\n")
      
      val outputFile = File("src/main/java/com/example/data/models/GaslandsStaticData.kt")
      outputFile.parentFile?.mkdirs()
      outputFile.writeText(sb.toString())
      println("Generated GaslandsStaticData.kt at local file system path: ${outputFile.absolutePath}")

      conn.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    println("--- END FILE GENERATING ---")
  }
}
