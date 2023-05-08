package com.bignerdranch.android.nerdlauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "NerdLauncherActivity"
class NerdLauncherActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nerd_launcher)

        recyclerView = findViewById(R.id.app_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupAdapter()
    }

    private fun setupAdapter() {

        //создание неявного интента
        val startupIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER) //пересеная добавленая в категории интента
        }
        val activities = packageManager.queryIntentActivities(startupIntent, 0)  /*возвращает список содержащий
        ResolveInfo для всех активити у которых есть фильтр соответстующий данному интенту
        --flags - сюда можно добавить влаги для измения результата если 0 то это значит мы не меняем результат
        (например можно добавить дополнительнве данные(пути к библиотекам))*/

        activities.sortWith(Comparator { a, b ->
            String.CASE_INSENSITIVE_ORDER.compare(
                a.loadLabel(packageManager).toString(),
                b.loadLabel(packageManager).toString()
            )
        }) /*сортировка обьектов ResolveInfo в алфавитном порядке меток*/

        Log.i(TAG, "Found ${activities.size} activities")

        /*здесь мы создаем неявный интент с заданым действием ACTION_MAIN*/

        recyclerView.adapter = ActivityAdapter(activities)

    }

    private class ActivityHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener{
        //холдер для отображения метки активити

        private val nameTextView = itemView as TextView
        private lateinit var resolveInfo: ResolveInfo

        init {
            nameTextView.setOnClickListener(this)
        }

        fun bindActivity(resolveInfo: ResolveInfo) {
            this.resolveInfo = resolveInfo
            val packageManager = itemView.context.packageManager
            val appName = resolveInfo.loadLabel(packageManager).toString()
            nameTextView.text = appName
        }

        override fun onClick(view: View) {
            val activityInfo = resolveInfo.activityInfo

            //создаем явный интент
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName(activityInfo.applicationInfo.packageName,
                            activityInfo.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) /*флаг дает возможность запустить вызваную активити
                                                        в новой задаче()т.е. в стеке она будет отображаться как отдельная вкладка*/
            }

            val context = view.context
            context.startActivity(intent)
        }
    }

    private class ActivityAdapter(val activities: List<ResolveInfo>) :
        RecyclerView.Adapter<ActivityHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.
                    inflate(android.R.layout.simple_list_item_1, parent, false)
            return  ActivityHolder(view)
        }

        override fun onBindViewHolder(holder: ActivityHolder, position: Int) {
            val resolveInfo = activities[position]
            holder.bindActivity(resolveInfo)
        }

        override fun getItemCount(): Int {
            return activities.size
        }
    }
}