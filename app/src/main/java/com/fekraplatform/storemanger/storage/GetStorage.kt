
import android.content.Context
import android.content.SharedPreferences
import com.fekraplatform.storemanger.application.MyApplication


class GetStorage(private val inventoryName:String) {
    private val context = MyApplication.AppContext
    fun getData(key:String):String{
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(inventoryName,
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getString(key,"").toString()
    }

    fun setData(key:String, data: String){
        val sharedPreferences: SharedPreferences =context.getSharedPreferences(inventoryName,
            Context.MODE_PRIVATE
        )
        sharedPreferences.edit().putString(key,data).apply()
    }

    fun clearData(){
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(inventoryName,
            Context.MODE_PRIVATE
        )
        sharedPreferences.edit().clear().apply()
    }

}