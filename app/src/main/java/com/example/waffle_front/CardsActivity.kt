package com.example.waffle_front


import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.Layout
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class CardsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cards)

        // Кнопка назад и её обработчик
        val backFromCards: Button = findViewById(R.id.backFromMyCards)
        backFromCards.setOnClickListener{
            finish()
        }

        // переменные для работы с картами и группами
        var usingFileName: String = "error"
        var groupPositionExpand: Int = -1
        var childPositionClicked: Int = -1

        val listAdapter: CustomExpandableListAdapter

        // Спики карт и остальное что нужно для них
        val expandableListView: ExpandableListView = findViewById(R.id.expandableListView)
        val listGroup: MutableList<String> = ArrayList()
        val listItem: MutableMap<String, MutableList<String>> = HashMap()
        listAdapter = CustomExpandableListAdapter(this, listGroup, listItem)
        expandableListView.setAdapter(listAdapter)

        // Кнопка добавления набора карт в список и её обработчик
        val addGroup: Button = findViewById(R.id.addGroupButton)
        addGroup.setOnClickListener{
            // TODO: Добавить текстовое поле с именем группы в формате всплываещего окна (или как это называется)
            // времененное именование наборов
            val newGroup = "Новая группа ${listGroup.size + 1}"
            listGroup.add(newGroup)
            listItem[newGroup] = ArrayList()
            listAdapter.notifyDataSetChanged()
        }

        // Кнопка добавления карт в группу и её обработчик
        val addItem: Button = findViewById(R.id.addItemButton)
        addItem.setOnClickListener{
            if (groupPositionExpand == -1){
                Toast.makeText(this, "Пожалуйста раскройте группу", Toast.LENGTH_SHORT).show()
            } else {
                var builder = AlertDialog.Builder(this)
                builder.setTitle("Введите название карты")
                val input = EditText(this)
                builder.setView(input)
                builder.setPositiveButton("OK") { dialog, which ->
                    val itemName = input.text.toString()
                    if (itemName.isNotEmpty()) {
                        if (listItem[listGroup[groupPositionExpand]] == null) {
                            listItem[listGroup[groupPositionExpand]] = mutableListOf(itemName)
                        } else { listItem[listGroup[groupPositionExpand]]?.add(itemName) }
                        listAdapter.notifyDataSetChanged()
                    } else {
                    Toast.makeText(this, "Название карты не может быть пустым", Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton("Отмена") { dialog, which ->
                    dialog.cancel()
                }
                builder.show()
            }
        }

        // Кнопка удаления карты из набора и её обработчик
        val deleteItem: Button = findViewById(R.id.deleteItemButton)
        deleteItem.setOnClickListener {
            if (groupPositionExpand == -1 || childPositionClicked == -1) {
                Toast.makeText(this, "Пожалуйста выберите карту для удаления", Toast.LENGTH_SHORT).show()
            } else {
                listItem[listGroup[groupPositionExpand]]?.removeAt(childPositionClicked)
                listAdapter.notifyDataSetChanged()
                childPositionClicked = -1
            }
        }

        // Фукция для выставления выбранных групп
        expandableListView.setOnGroupClickListener { parent, v, groupPosition, id ->
            if (expandableListView.isGroupExpanded(groupPosition)){
                expandableListView.collapseGroup(groupPosition)
                childPositionClicked = -1
                groupPositionExpand = -1
            } else {
                for (i in 0 until listAdapter.groupCount){
                    expandableListView.collapseGroup(i)
                }
                expandableListView.expandGroup(groupPosition)
                groupPositionExpand = groupPosition
            }
            true
        }

        // Функция Выобора карты
        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            childPositionClicked = childPosition
            true
        }

        // Функция для заполнения списка карт из файла
        fun initListData(resFileName: Int, cardFileName: String) {
            // TODO: Прописать добавления всего из файлов
            listGroup.add("Базовый набор")
            val cards = mutableListOf<String>()

            val reader: BufferedReader
            val file = File(filesDir, cardFileName)
            // если пользователь до этого не сохранял карты то загружаем стандатные наборы
            if (file.exists()) {
                val inputStream = openFileInput(cardFileName)
                reader = BufferedReader(InputStreamReader(inputStream))
                reader.useLines { lines ->
                    lines.forEach { line ->
                        cards.add(line)
                    }
                }
            } else {
                val inputStream = resources.openRawResource(resFileName)
                reader = BufferedReader(InputStreamReader(inputStream))
                reader.useLines { lines ->
                    lines.forEach { line ->
                        cards.add(line)
                    }
                }
            }

            listItem[listGroup[0]] = cards

            reader.close()
            listAdapter.notifyDataSetChanged()
        }

        // Функция сохранения данных в файл
        fun saveListData(fileName: String) {
            if (usingFileName == "error"){
                return
            }

            try {
                val outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
                val writer = outputStream.bufferedWriter()
                listGroup.forEach { group ->
                    listItem[group]?.forEach { item ->
                        writer.write("$item\n")
                    }
                }
                writer.close()
                outputStream.close()

                Toast.makeText(this, "Данные успешно сохранены в $fileName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка при сохранении данных: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
            usingFileName = "error"
        }

        // Виджет со списком карт
        val listLayout: View = findViewById(R.id.cardsList)

        // Затемнение фона
        val dimBackground: View = findViewById(R.id.dimBackground)
        dimBackground.setOnClickListener {
            saveListData(usingFileName)
            dimBackground.visibility = View.GONE
            listLayout.visibility = View.GONE
            listGroup.clear()
            listItem.clear()
            listAdapter.notifyDataSetChanged()
        }

        // Вызов списка с картами действий
        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            listLayout.visibility = View.VISIBLE
            usingFileName = "action_cards.txt"
            initListData(R.raw.action_cards, "action_cards.txt")
        }

        // Вызов списка с картами ролей
        val button2: Button = findViewById(R.id.button2)
        button2.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            listLayout.visibility = View.VISIBLE
            usingFileName = "role_cards.txt"
            initListData(R.raw.role_cards, "role_cards.txt")
        }

        // Вызов списка с картами настроений
        val button3: Button = findViewById(R.id.button3)
        button3.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            listLayout.visibility = View.VISIBLE
            usingFileName = "mood_cards.txt"
            initListData(R.raw.mood_cards, "mood_cards.txt")
        }

        // Вызов списка с картами ситуций
        val button4: Button = findViewById(R.id.button4)
        button4.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            listLayout.visibility = View.VISIBLE
            usingFileName = "situation_cards.txt"
            initListData(R.raw.situation_cards, "situation_cards.txt")
        }

        val saveCardData: Button = findViewById(R.id.saveCardsButton)
        saveCardData.setOnClickListener {
            saveListData(usingFileName)
            dimBackground.visibility = View.GONE
            listLayout.visibility = View.GONE
            listGroup.clear()
            listItem.clear()
            listAdapter.notifyDataSetChanged()
        }

    }
}

// Adpter для списка. Используется так же как в свинге мы писали адаптер для таблицы
class CustomExpandableListAdapter(
    private val context: Context,
    private val listGroup: List<String>,
    private val listItem: MutableMap<String, MutableList<String>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = listGroup.size

    override fun getChildrenCount(groupPosition: Int): Int = listItem[listGroup[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = listGroup[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = listItem[listGroup[groupPosition]]?.get(childPosition) ?: ""

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val groupTitle = getGroup(groupPosition) as String
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = groupTitle
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val childTitle = getChild(groupPosition, childPosition) as String
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_2, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = childTitle
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}

