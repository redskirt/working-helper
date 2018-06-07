package com.sasaki.chain

/**
 * @Author Sasaki
 * @Mail redskirt@outlook.com
 * @Timestamp Jun 7, 2018 11:05:19 PM
 * @Description 
 */
trait ScalaEntity[E] {
  
  protected def set[T](e: E, $attr: String, $value: T): E = {
    
    e
  }
  
}