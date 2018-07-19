package com.sasaki.chain

import com.sasaki.packages.independent._
import com.sasaki.packages.constant._
import java.lang.reflect.Method

/**
 * @Author Sasaki
 * @Mail redskirt@outlook.com
 * @Timestamp Jun 7, 2018 11:05:19 PM
 * @Description 
 */
trait ScalaEntity {
  
   final val CLASS_STRING = classOf[String]
   final val CLASS_OBJECT = classOf[Object]
  
  /**
   * 该方法暂不适用对父类继承属性赋值
   */
  protected def set[E](e: E, attr: String, value: Object, /*allowNul: Boolean = false, */ classOfValue: Class[_] = null): E = {

    var method: Method = null
    if (isNull(classOfValue)) 
      // 无classOfValue参数时，默认提供value空值约束，value.getClass可用
      invokeVerify(nonNull(e) && nonNull(attr) && nonNull(value), "Entity/attr/value must be non-Nul.") { () =>
        method = e.getClass().getMethod(attr + "_$eq", value.getClass)
      }
    else
      // 不进行value空值检验，此时value允许赋空值，
      // 但是必须显示提供value类型，因为无法从null得到value的类型。
      invokeVerify(nonNull(e) && nonNull(attr), "Entity/attr must be non-Nul.") { () =>
        method = e.getClass().getMethod(attr + "_$eq", classOfValue)
      }

    method.invoke(e, value)
    e
  }

  
  /**
   * 该方法暂不适用对父类继承属性赋值
   */
  protected def setMultiple[E](e: E, attrs_values: Seq[Tuple2[String, Object]]): E =
    invokeVerify(nonNull(e) && nonNull(attrs_values), "Entity/attrs_values must be non-Nul.") { () =>
      attrs_values.foreach(o => set(e, o._1, o._2))
      e
    }
  
  /**
   * 该方法暂不适用对父类继承属性赋值
   */
  protected def setMultiple_3[E](e: E, attrs_values: Seq[Tuple3[String, Object, Class[_]]]): E = {
    attrs_values.foreach(o => set(e, o._1, o._2, o._3))
    e
  }
  
  // TODO ??? 使用Scala反射API方法实现
  protected def toString[E](e: E) = {
    val builder = new StringBuilder(e.getClass().getName() + " --> ")
		val fields = e.getClass().getDeclaredFields()
		
		for (i <- 0 until fields.length)
			builder.append("{" + fields(i).getName() + "=" + fields(i).get(e).toString() + "}, ")
		
		builder.toString()
  }
}

class A {
  var id: JInt = _
  var attr: String = _
  var date: java.sql.Date = _
}
  
object Main extends App with ScalaEntity {
  
  val a = new A
  
  a.getClass.getMethods.foreach(println)
//  setMultiple[A](a, Array(
//    ("id", Int.box(2)),
//    ("attr", "123123")
//  ))
  
//  set(a, "attr", null, classOf[String])
//  set(a, "attr", "ssss")
  
//  println(a.attr + " " + a.id)
}

