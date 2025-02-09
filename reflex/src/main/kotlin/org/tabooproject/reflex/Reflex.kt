package org.tabooproject.reflex

@Suppress("UNCHECKED_CAST")
class Reflex {

    /**
     * 为了兼容性，保持原有的形势
     */
    companion object {

        /**
         * 已注册的 ReflexRemapper
         * 直接添加到改容器即可完成注册，起初用于转换 1.17 版本的混淆字段名称
         */
        val remapper = ArrayList<ReflexRemapper>()

        /**
         * 不通过构造函数实例化对象
         */
        fun <T> Class<T>.unsafeInstance(): Any {
            return UnsafeAccess.unsafe.allocateInstance(this)!!
        }

        /**
         * 通过构造方法实例化对象
         */
        fun <T> Class<T>.invokeConstructor(vararg parameter: Any?): T {
            return ReflexClass.of(this).getConstructor(*parameter).instance(*parameter) as T
        }

        /**
         * 执行方法
         * @param name 方法名称
         * @param parameter 方法参数
         * @param fixed 是否为静态方法
         */
        fun <T> Any.invokeMethod(name: String, vararg parameter: Any?, fixed: Boolean = false): T? {
            return if (fixed && this is Class<*>) {
                ReflexClass.of(this).getMethod(name, true, *parameter).invokeStatic(*parameter) as T?
            } else {
                ReflexClass.of(javaClass).getMethod(name, true, *parameter).invoke(this, *parameter) as T?
            }
        }

        /**
         * 获取字段
         * @param path 字段名称，使用 "/" 符号进行递归获取
         * @param isStatic 是否为静态字段
         */
        fun <T> Any.getProperty(path: String, isStatic: Boolean = false): T? {
            return if (path.contains('/')) {
                getLocalProperty<Any>(path.substringBefore('/'), isStatic)?.getProperty(path.substringAfter('/'), isStatic)
            } else {
                getLocalProperty(path, isStatic)
            }
        }

        /**
         * 修改字段
         * @param path 字段名称，使用 "/" 符号进行递归获取
         * @param value 值
         * @param fixed 是否为静态字段
         */
        fun Any.setProperty(path: String, value: Any?, fixed: Boolean = false) {
            if (path.contains('/')) {
                getLocalProperty<Any>(path.substringBefore('/'), fixed)!!.setProperty(path.substringAfter('/'), value, fixed)
            } else {
                setLocalProperty(path, value, fixed)
            }
        }

        private fun <T> Any.getLocalProperty(name: String, fixed: Boolean = false): T? {
            return if (fixed && this is Class<*>) {
                ReflexClass.of(this).getField(name, true).get() as T?
            } else {
                ReflexClass.of(javaClass).getField(name, true).get(this) as T?
            }
        }

        private fun Any.setLocalProperty(name: String, value: Any?, fixed: Boolean = false) {
            if (fixed && this is Class<*>) {
                ReflexClass.of(this).getField(name, true).setStatic(value)
            } else {
                ReflexClass.of(javaClass).getField(name, true).set(this, value)
            }
        }
    }
}