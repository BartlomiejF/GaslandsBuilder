package com.bartek.gaslandsbuilder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bartek.gaslandsbuilder.data.Perk
import com.bartek.gaslandsbuilder.data.getAllPerks
import com.bartek.gaslandsbuilder.data.microPlateArmourPerk
import com.bartek.gaslandsbuilder.data.prisonCarPerk
import com.bartek.gaslandsbuilder.databinding.SinglePerkRowBinding

class addPerk : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_perk)
        val sumCost: Int = intent.extras!!.getInt("cost")
        findViewById<TextView>(R.id.addPerkSumCarCost).text = sumCost.toString()
        var perks: MutableList<Perk> = getAllPerks(application)
        val sponsor: String = intent.extras!!.getString("sponsor", "Custom")
        when (sponsor){
            "The Warden" -> perks.add(0, prisonCarPerk)
//            "The Warden" -> perks.add(0, Perk("Prison Car", "Sponsored Perk", -4))
            "Verney" -> perks.add(0, microPlateArmourPerk)
//            "Verney" -> perks.add(0, Perk("MicroPlate Armour", "Sponsored Perk", 6))
        }
        val carUpgradesRecyclerView: RecyclerView = findViewById(R.id.perksView)
        carUpgradesRecyclerView.apply {
            layoutManager = LinearLayoutManager(application)
            adapter = CarPerksAdapter(
                perks,
                ::addPerk
            )
        }
    }

    fun addPerk(perk: Perk) {
        val intent = Intent()
        intent.putExtra("chosenPerk", perk)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}

class CarPerksAdapter(
    val perksList: MutableList<Perk>,
    val perkAdder:(Perk) -> Unit
): RecyclerView.Adapter<CarPerksAdapter.ViewHolder>() {

    class ViewHolder(view: SinglePerkRowBinding): RecyclerView.ViewHolder(view.root) {
        val binding = view
        fun bind(perk: Perk, perkAdder: (Perk) -> Unit){
            binding.perkName.text = perk.name
            binding.perkCost.text = "${perk.cost} cans"
            binding.perkClass.text = perk.perkClass
            binding.root.setOnClickListener{
                perkAdder(perk)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.single_perk_row, parent, false)
            val view = SinglePerkRowBinding.inflate(inflater, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return perksList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(perksList[position], perkAdder)
    }
}
