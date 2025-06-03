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
import org.json.JSONArray
import org.json.JSONObject
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
        // Устанавливаем выбор только одного элемента
        expandableListView.choiceMode = ExpandableListView.CHOICE_MODE_SINGLE

        // Кнопка добавления набора карт в список и её обработчик
        val addGroup: Button = findViewById(R.id.addGroupButton)
        addGroup.setOnClickListener{
            var builder = AlertDialog.Builder(this)
            builder.setTitle("Введите название набора")
            val input = EditText(this)
            builder.setView(input)
            builder.setPositiveButton("OK") { dialog, which ->
                val groupName = input.text.toString()
                if (groupName.isNotEmpty()) {
                    listGroup.add(groupName)
                    listItem[groupName] = ArrayList()
                    listAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "Название набора не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Отмена") { dialog, which ->
                dialog.cancel()
            }
            builder.show()
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
        val deleteGroup: Button = findViewById(R.id.deleteGroupButton)
        deleteGroup.setOnClickListener {
            if (groupPositionExpand == -1) {
                Toast.makeText(this, "Пожалуйста раскройте группу которою хотите удалить", Toast.LENGTH_SHORT).show()
            } else {
                var builder = AlertDialog.Builder(this)
                var text = listGroup.get(groupPositionExpand)
                builder.setTitle("Хотите удалить набор $text?")
                builder.setPositiveButton("Да") { dialog, which ->
                    val itemName = "12345"
                    listItem[listGroup[groupPositionExpand]]?.clear()
                    listGroup.removeAt(groupPositionExpand)
                    listAdapter.notifyDataSetChanged()
                }
                builder.setNegativeButton("Нет") { dialog, which ->
                    dialog.cancel()
                }
//                builder.setNegativeButton("Удалить") {dialog, witch ->
//                    listItem[listGroup[groupPositionExpand]]?.removeAt(childPositionClicked)
//                    listAdapter.notifyDataSetChanged()
//                }
                builder.show()
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

        // Функция измения или удаления карты при нажатии на нее
        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            (parent.expandableListAdapter as CustomExpandableListAdapter)
                .setSelectedPosition(groupPosition, childPosition)
            childPositionClicked = childPosition
            groupPositionExpand = groupPosition

            var dialogView = LayoutInflater.from(this).inflate(R.layout.change_or_remove_card, null)
            var builder = AlertDialog.Builder(this)
            builder.setTitle("Хотите изменить или удалить карту?")
            builder.setView(dialogView)
            val input = dialogView.findViewById<EditText>(R.id.cardName)
            var text = listItem[listGroup[groupPositionExpand]]?.get(childPositionClicked)
            input.setText(text)
            builder.setPositiveButton("Изменить") { dialog, which ->
                val itemName = input.text.toString()
                if (itemName.isNotEmpty()) {
                    listItem[listGroup[groupPositionExpand]]?.set(childPositionClicked, itemName)
                    listAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "Название карты не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNeutralButton("Отмена") { dialog, which ->
                dialog.cancel()
            }
            builder.setNegativeButton("Удалить") {dialog, witch ->
                listItem[listGroup[groupPositionExpand]]?.removeAt(childPositionClicked)
                listAdapter.notifyDataSetChanged()
            }
            builder.show()

            true
        }

        // Функция для заполнения списка карт из файла
        fun initListData(resFileName: Int, cardFileName: String) {
            val reader: BufferedReader
            val file = File(filesDir, cardFileName)
            if (file.exists()){
                try {
                    val inputStream = openFileInput(cardFileName)
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    inputStream.close()
                    val jsonObject = JSONObject(jsonString)

                    listGroup.clear()
                    listItem.clear()

                    jsonObject.keys().forEach { group ->
                        listGroup.add(group) // Добавляем группу в список
                        val jsonArray = jsonObject.getJSONArray(group)
                        val items = mutableListOf<String>()

                        for (i in 0 until jsonArray.length()) {
                            items.add(jsonArray.getString(i))
                        }

                        listItem[group] = items // Записываем соответствующие элементы в карту
                    }
//                    Toast.makeText(this, "Данные успешно загружены из $cardFileName", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка при загрузке данных: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
            else {
                listGroup.add("Базовый набор")
                val cards = mutableListOf<String>()
                val inputStream = resources.openRawResource(resFileName)
                reader = BufferedReader(InputStreamReader(inputStream))
                reader.useLines { lines ->
                    lines.forEach { line ->
                        cards.add(line)
                    }
                }
                listItem[listGroup[0]] = cards
            }
            listAdapter.notifyDataSetChanged()
        }


//        fun initListData(resFileName: Int, cardFileName: String) {
//            // TODO: Прописать добавления всего из файлов
//            listGroup.add("Базовый набор")
//            val cards = mutableListOf<String>()
//
//            val reader: BufferedReader
//            val file = File(filesDir, cardFileName)
//            // если пользователь до этого не сохранял карты то загружаем стандатные наборы
//            if (file.exists()) {
//                val inputStream = openFileInput(cardFileName)
//                reader = BufferedReader(InputStreamReader(inputStream))
//                reader.useLines { lines ->
//                    lines.forEach { line ->
//                        cards.add(line)
//                    }
//                }
//            } else {
//                val inputStream = resources.openRawResource(resFileName)
//                reader = BufferedReader(InputStreamReader(inputStream))
//                reader.useLines { lines ->
//                    lines.forEach { line ->
//                        cards.add(line)
//                    }
//                }
//            }
//
//            listItem[listGroup[0]] = cards
//
//            reader.close()
//            listAdapter.notifyDataSetChanged()
//        }

        // Функция сохранения данных в файл
        fun saveListData(fileName: String) {
            if (usingFileName == "error") {
                return
            }

            try {
                val jsonObject = JSONObject()
                listGroup.forEach { group ->
                    val jsonArray = JSONArray()
                    listItem[group]?.forEach { item ->
                        jsonArray.put(item)
                    }
                    jsonObject.put(group, jsonArray)
                }

                val jsonString = jsonObject.toString(4) // Форматированный JSON
                val outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
                outputStream.bufferedWriter().use { it.write(jsonString) }
                outputStream.close()

                Toast.makeText(this, "Данные успешно сохранены в $fileName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка при сохранении данных: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
            usingFileName = "error"
        }


//        fun saveListData(fileName: String) {
//            if (usingFileName == "error"){
//                return
//            }
//
//            try {
//                val outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
//                val writer = outputStream.bufferedWriter()
//                listGroup.forEach { group ->
//                    listItem[group]?.forEach { item ->
//                        writer.write("$item\n")
//                    }
//                }
//                writer.close()
//                outputStream.close()
//
//                Toast.makeText(this, "Данные успешно сохранены в $fileName", Toast.LENGTH_SHORT).show()
//            } catch (e: Exception) {
//                Toast.makeText(this, "Ошибка при сохранении данных: ${e.message}", Toast.LENGTH_SHORT).show()
//                e.printStackTrace()
//            }
//            usingFileName = "error"
//        }

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
            usingFileName = "action_cards.json"
            initListData(R.raw.action_cards, usingFileName)
        }

        // Вызов списка с картами ролей
        val button2: Button = findViewById(R.id.button2)
        button2.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            listLayout.visibility = View.VISIBLE
            usingFileName = "role_cards.json"
            initListData(R.raw.role_cards, usingFileName)
        }

        // Вызов списка с картами настроений
        val button3: Button = findViewById(R.id.button3)
        button3.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            listLayout.visibility = View.VISIBLE
            usingFileName = "mood_card.json"
            initListData(R.raw.mood_cards, usingFileName)
        }

        // Вызов списка с картами ситуций
        val button4: Button = findViewById(R.id.button4)
        button4.setOnClickListener {
            dimBackground.visibility = View.VISIBLE
            listLayout.visibility = View.VISIBLE
            usingFileName = "situation_cards.json"
            initListData(R.raw.situation_cards, usingFileName)
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

    private var selectedGroupPosition: Int = -1
    private var selectedChildPosition: Int = -1

    fun setSelectedPosition(groupPosition: Int, childPosition: Int) {
        selectedGroupPosition = groupPosition
        selectedChildPosition = childPosition
        notifyDataSetChanged()
    }

    override fun getGroupCount(): Int = listGroup.size

    override fun getChildrenCount(groupPosition: Int): Int = listItem[listGroup[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = listGroup[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = listItem[listGroup[groupPosition]]?.get(childPosition) ?: ""

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val groupTitle = getGroup(groupPosition) as String
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.list_item_text)
        textView.text = groupTitle
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val childTitle = getChild(groupPosition, childPosition) as String
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.list_item_text)
        textView.text = childTitle
        // Устанавливаем состояние активации
//        view.isActivated = (groupPosition == selectedGroupPosition && childPosition == selectedChildPosition)

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}

