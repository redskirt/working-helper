package com.sasaki.chain;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @Author Wei Liu
 * @Mail wei.liu@suanhua.org
 * @Timestamp 2017年4月7日 上午10:27:56
 * @Description
 * 
 */
public class Entity<E> {
	private final static String SET = "set";

	/**
	 * 链式set方法，单个属性
	 * 
	 * @param e
	 * @param _attr_
	 * @param _value_
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	protected <T> E set(E e, String _attr_, T _value_) throws Exception {
		if (isEmpty(e) || isEmpty(_attr_) || isEmpty(_value_)) throw new NullPointerException("Entity/_attr_/_value_ isNul.");
		
		Method method = e.getClass().getMethod(SET + capitalize(_attr_)/*拼接JavaBean Setter*/, _value_.getClass());
		method.invoke(e, _value_);
		
		return e;
	}

	/**
	 * 链式set方法，多个属性且保证传入两个集合参数个数相同
	 * @param e
	 * @param _attrs_
	 * @param _values_
	 * @return
	 * @throws Exception
	 */
	protected <T> E setMultiple(E e, T[][] _attrs_values_) throws Exception {
		if(isEmpty(_attrs_values_) || _attrs_values_ .length != 2 || _attrs_values_[0].length != _attrs_values_[1].length)
			throw new Exception("Object[][] _attrs_$attrs_ isNul OR _attrs_ not matched _$attrs_.");

		for (int i = 0; i < _attrs_values_[0].length; i++) 
			set(e, String.valueOf(_attrs_values_[0][i]), _attrs_values_[1][i]);

		return e;
	}
	
	protected static <T> String toString(T e) throws Exception {
		StringBuilder builder = new StringBuilder(e.getClass().getName() + " --> ");
		Field[] fields = e.getClass().getDeclaredFields();
		Field.setAccessible(fields, true);
		
		for (int i = 0; i < fields.length; i++)
			builder.append("{" + fields[i].getName() + "=" + fields[i].get(e).toString() + "}, ");
		
		return builder.toString();
	}
	
	protected boolean isEmpty(Object object) {
		if (object == null) 
			return true;
		if (object instanceof String && object.toString().trim().length() == 0) 
			return true;
		if (object.getClass().isArray() && Array.getLength(object) == 0) 
			return true;
		if (object instanceof Collection && ((Collection<?>) object).isEmpty()) 
			return true;
		if (object instanceof Map && ((Map<?, ?>) object).isEmpty()) 
			return true;
		
		return false;
	}
	
	private static String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
            .append(Character.toTitleCase(str.charAt(0)))
            .append(str.substring(1))
            .toString();
    }
}
