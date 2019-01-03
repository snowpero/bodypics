package com.ninis.camera_sample.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ninis.camera_sample.DEF_DIR_PATH
import com.ninis.camera_sample.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.android.synthetic.main.fragment_pic_calendar.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class PicCalendarFragment : Fragment() {

    private val imgFileList: ArrayList<File> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_pic_calendar, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadFileList()
        initCalendar()
    }

    private fun initCalendar() {
        val eventDays = ArrayList<CalendarDay>()
        for( file in imgFileList ) {
            val calendarDay = CalendarDay.from(Date(file.lastModified()))
            eventDays.add(calendarDay)
        }

        calendar_view.setDateSelected(Calendar.getInstance(), true)
        calendar_view.addDecorator(EventDecorator(Color.GREEN, eventDays))
        calendar_view.setOnDateChangedListener(OnDateSelectedListener {
            materialCalendarView, calendarDay, b ->

        })
    }

    private fun loadFileList() {
        val dirInfo = File(DEF_DIR_PATH)
        if( dirInfo.exists() ) {
            if( imgFileList.isNotEmpty() )
                imgFileList.clear()

            for( file in dirInfo.listFiles() ) {
                imgFileList.add(file)
            }
        }
    }

    inner class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {
        private val dates: HashSet<CalendarDay>

        init {
            this.dates = HashSet(dates)
        }

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(5f, color))
        }
    }
}