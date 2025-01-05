package com.example.waffle_front


import android.os.Bundle
import android.widget.Button
import android.widget.ExpandableListView
import androidx.appcompat.app.AppCompatActivity

class CardsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cards)

        // Кнопка назад и её обработчик
        val backFromCards: Button = findViewById(R.id.backFromMyCards)
        backFromCards.setOnClickListener{
            finish()
        }


        // Спики карт и остальное что нужно для них
        val expandableListView: ExpandableListView
        val listGroup: MutableList<String>
        val listItem: MutableMap<String, MutableList<String>>

        expandableListView = findViewById(R.id.expandableListView)
        listGroup = ArrayList()
        listItem = HashMap()

        // Кнопка добавления набора карт в список и её обработчик
        val addGroup: Button = findViewById(R.id.addGroupButton)
        addGroup.setOnClickListener{
            // TODO: Добавить текстовое поле с именем группы в формате всплываещего окна (или как это называется)
            // времененное именование наборов
            val newGroup = "Новая группа ${listGroup.size + 1}"
            listGroup.add(newGroup)
            listItem[newGroup] = ArrayList()
        }

        fun initListData() {
            // TODO: Прописать добавления всего из файлов
            listGroup.add("Группа 1")
            listGroup.add("Группа 2")
            val group1 = ArrayList<String>()
            group1.add("Элемент 1")
            group1.add("Элемент 2")
            val group2 = ArrayList<String>()
            group2.add("Элемент 1")
            group2.add("Элемент 2")
            listItem[listGroup[0]] = group1
            listItem[listGroup[1]] = group2
            //listAdapter.notifyDataSetChanged()
        }
        initListData()


    }


}