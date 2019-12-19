# ASM_Demo


## 1、ASM概述
* ASM是一个功能比较齐全的java字节码操作与分析框架，通过ASM框架，我们可以动态的生成类或者增强已有类的功能。
* ASM可以直接生成二进制.class文件，也可以在类被加载入java虚拟机之前动态改变现有类的行为。
* java的二进制文件被存储在严格格式定义的.class文件里，这些字节码文件拥有足够的元数据信息用来表示类中的所有元素，包括类名称、方法、属性以及java字节码指令。ASM从字节码文件读入这些信息后，能够改变类行为、分析类的信息，甚至还可以根据具体的要求生成新的类。
* ASM 通过树这种数据结构来表示复杂的字节码结构，因为需要处理字节码结构是固定的，所以可以利用Visitor(访问者) 设计模式来对树进行遍历，在遍历过程中对字节码进行修改。

## 2、Java 类文件概述
所谓 Java 类文件，就是通常用 javac 编译器产生的 .class 文件。这些文件具有严格定义的格式。Java 源文件经过 javac 编译器编译之后，将会生成对应的二进制文件。

Java 类文件是 8 位字节的二进制流。数据项按顺序存储在 class 文件中，相邻的项之间没有间隔，这使得 class 文件变得紧凑，减少存储空间。一个简单的Hello World程序
```
public class HelloWorld { 
    public static void main(String[] args) { 
        System.out.println("Hello world"); 
    } 
}
```
经过 javac 编译后，得到的类文件HelloWorld.class，该文件中是由十六进制符号组成的，这一段十六进制符号组成的长串是严格遵守 Java 虚拟机规范。用vim查看HelloWorld.class
```
vim HelloWorld.class
```
打开文件后输入
```
:%!xxd
```
按回车即可看到如下一串串十六进制符号

![](https://upload-images.jianshu.io/upload_images/9513946-0ca08211c10b93bc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/300)



HelloWorld.class文件构成如下：
![](https://upload-images.jianshu.io/upload_images/9513946-8a8634803ce4e491.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800)


从上图中可以看到，一个 Java 类文件大致可以归为 10 个项：
* **Magic：**该项存放了一个 Java 类文件的魔数（magic number），一个 Java 类文件的前 4 个字节被称为它的魔数。每个正确的 Java 类文件都是以 0xCAFEBABE 开头的，这样保证了 Java 虚拟机能很轻松的分辨出 Java 文件和非 Java 文件。
  `有趣的是，魔数的固定值是Java之父James Gosling制定的，为CafeBabe（咖啡宝贝），而Java的图标为一杯咖啡。`
  
* **Version：**该项存放了 Java 类文件的版本信息
* **Constant Pool：**常量池中存储两类常量：字面量与符号引用。字面量为文本字符串和代码中声明为Final的常量值，符号引用如类和接口的全局限定名、字段的名称和描述符、方法的名称和描述符。
* **Access_flag：**该项指明了该文件中定义的是类还是接口（一个 class 文件中只能有一个类或接口），同时还指名了类或接口的访问标志，如 public，private, abstract 等信息。
* **This Class：**指向表示该类全限定名称的字符串常量的指针。
* **Super Class：**指向表示父类全限定名称的字符串常量的指针。
* **Interfaces：**一个指针数组，存放了该类或父类实现的所有接口名称的字符串常量的指针。
* **Fields：**该项对类或接口中声明的字段进行了细致的描述。需要注意的是，fields 列表中仅列出了本类或接口中的字段，并不包括从超类和父接口继承而来的字段。
* **Methods：**该项对类或接口中声明的方法进行了细致的描述。例如方法的名称、参数和返回值类型等。需要注意的是，methods 列表里仅存放了本类或本接口中的方法，并不包括从超类和父接口继承而来的方法。
* **Class attributes：**该项存放了在该文件中类或接口所定义的属性的基本信息。

## 3、ASM库的结构
* **Core：**为其他包提供基础的读、写、转化Java字节码和定义的API，并且可以生成Java字节码和实现大部分字节码的转换。

* **Tree：**提供了 Java 字节码在内存中的表现
* **Commons：**提供了一些常用的简化字节码生成、转换的类和适配器
* **Util：**包含一些帮助类和简单的字节码修改类，有利于在开发或者测试中使用
* **XML：**提供一个适配器将XML和SAX-comliant转化成字节码结构，可以允许使用XSLT去定义字节码转化


## 4、ASM Core API
* **ClassReader：**这个类会将 .class 文件读入到 ClassReader 中的字节数组中，它的 accept 方法接受一个 ClassVisitor 实现类，并按照顺序调用 ClassVisitor 中的方法

* **ClassVisitor：**主要负责访问类的成员信息。包括标记在类上的注解、类的构造方法、类的字段、类的方法、静态代码块等
* **ClassWriter：**ClassWriter 是一个 ClassVisitor 的子类，是和 ClassReader 对应的类，ClassReader 是将 .class 文件读入到一个字节数组中，ClassWriter 是将修改后的类的字节码内容以字节数组的形式输出。

* **AdviceAdapter：**MethodVisitor 是一个抽象类，当 ASM 的 ClassReader 读取到 Method 时就转入 MethodVisitor 接口处理。AdviceAdapter 是 MethodVisitor 的子类，使用 AdviceAdapter 可以更方便的修改方法的字节码。AdviceAdapter其中几个重要方法如下：
`void visitCode()`：表示 ASM 开始扫描这个方法
`void onMethodEnter()`：进入这个方法
`void onMethodExit()`：即将从这个方法出去
`void onVisitEnd()`：表示方法扫描完毕


我们来重点看下`ClassVisitor`类
`ClassVisitor`类的API如下
![image.png](https://upload-images.jianshu.io/upload_images/9513946-8e17c493e622c685.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 4.1 `visit`
```
    /**
     * 可以拿到类的详细信息
     *
     * @param version jdk的版本： 52 代表jdk版本 1.8；51 代表jdk版本 1.7
     * @param access 类的修饰符：ACC_PUBLIC、ACC_PRIVATE、ACC_PROTECTED、ACC_FINAL、ACC_SUPER
     * @param name 类的名称：以路径的形式表示 com/joker/demo/TestClass
     * @param signature 泛型信息：未定义泛型，则该参数为null
     * @param superName 表示当前类所继承的父类
     * @param interfaces 表示类所实现的接口列表
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
    }
```
**类的修饰符**
类的修饰符以“ACC_开头”，可以作用到类级别上的修饰符主要有下面这些

| 修饰符 | 含义 |
| --- | --- |
| ACC_PUBLIC | public |
| ACC_PRIVATE | private |
| ACC_PROTECTED | protected |
| ACC_FINAL | final |
| ACC_SUPER | extends |
| ACC_INTERFACE | 接口 |
| ACC_ABSTRACT | 抽象类 |
| ACC_ANNOTATION | 注解类型 |
| ACC_ENUM | 枚举类型 |
| ACC_DEPRECATED | 标记了@Deprecated注解的类 |
| ACC_SYNTHETIC | javac生成 |

### 4.2 `visitAnnotation`
```
    /**
     * 当扫描器扫描到类注解声明时进行调用
     *
     * @param desc 注解类型(签名类型)
     * @param visible 注解是否可以在 JVM 中可见
     * @return
     */
    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible)
    }
```

### 4.3 `visitField`
```
/**
     * 当扫描器扫描到类中字段时进行调用
     *
     * @param access 修饰符
     * @param name 字段名
     * @param desc 字段类型
     * @param signature 泛型描述
     * @param value 默认值
     * @return
     */
    @Override
    FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access, name, desc, signature, value)
    }
```

### 4.4 `visitMethod`
```
    /**
     * 当扫描器扫描到类的方法时调用
     *
     * @param access 方法的修饰符
     * @param name 方法名
     * @param desc 方法签名
     * @param signature 表示泛型相关的信息
     * @param exceptions 表示将会抛出的异常，如果方法没有抛出异常，则参数为空
     * @return
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return super.visitMethod(access, name, desc, signature, exceptions)
    }
```
**方法的修饰符**
可以作用到方法级别上的修饰符主要有下面这些

| 修饰符 | 含义 |
| --- | --- |
| ACC_PUBLIC | public |
| ACC_PRIVATE | private |
| ACC_PROTECTED | protected |
| ACC_STATIC | static |
| ACC_FINAL | final |
| ACC_SYNCHRONIZED | 同步的 |
| ACC_VARARGS | 不定参数个数的方法 |
| ACC_NATIVE | native类型方法 |
| ACC_ABSTRACT | 抽象的方法 |
| ACC_DEPRECATED | 标记了@Deprecated注解的类 |
| ACC_SYNTHETIC | javac生成 |

**方法的签名格式**
`(参数列表)返回值类型`

在ASM中不同的类型对应不同的代码，详细的对应关系如下表

| 代码 | 类型 |
| --- | --- |
| I | int |
| B | byte |
| C | char |
| D | double |
| F | float |
| J | long |
| S | short |
| Z | boolean |
| V | void |
| [...; | 数组 |
| [[...; | 二维数组 |
| [[[...; | 三维数组 |

方法参数列表对应的方法签名示例如下

| 参数列表 | 方法参数 |
| --- | --- |
| String[] | [Ljava/lang/String; |
| String[][] | [[Ljava/lang/String; |
| int，String，String[] | ILjava/lang/String;[Ljava/lang/String; |
| int，boolean，long，String[]，double | IZJ[Ljava/lang/String;D |
| Class<?>, String, Object...paramType | Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Object; |
| int[] | [I |

### 4.5 `visitEnd`
```
    /**
     * 当扫描器完成类扫描时才会调用
     */
    @Override
    void visitEnd() {
        super.visitEnd()
    }
```

## 5、ASM练手demo实现统计方法时长代码插桩
#### 5.1添加ASM依赖
```
implementation 'org.ow2.asm:asm-all:5.2'
```
#### 5.2定义一个HelloWorld类
```
public class HelloWorld {

    public void sayHello() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
#### 5.3通过`javac`命令执行`HelloWorld.java`得到`HelloWorld.class`

![](https://upload-images.jianshu.io/upload_images/9513946-6d98a002eed60b83.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/550)


目前桌面上已经生成了`HelloWorld.class`字节码文件
![](https://upload-images.jianshu.io/upload_images/9513946-ee1a95a274741466.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/200)


#### 5.4新建一个ASMTest类，从桌面读取`HelloWorld.class`文件，通过ASM读取`HelloWorld.class`文件，并将打印`sayHello()`方法调用时长的代码插桩到`sayHello()`方法中，输出新的字节码文件`OutputHelloWorld.class`到桌面
```
public class ASMTest {

    public static void redefineHelloWorldClass() {
        try {
            InputStream inputStream = new FileInputStream("/Users/jokerwan/Desktop/HelloWorld.class");
            // 1. 创建 ClassReader 读入 .class 文件到内存中
            ClassReader reader = new ClassReader(inputStream);
            // 2. 创建 ClassWriter 对象，将操作之后的字节码的字节数组回写
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            // 3. 创建自定义的 ClassVisitor 对象
            ClassVisitor change = new ChangeVisitor(writer);
            // 4. 将 ClassVisitor 对象传入 ClassReader 中
            reader.accept(change, ClassReader.EXPAND_FRAMES);

            System.out.println("Success!");
            // 获取修改后的 class 文件对应的字节数组
            byte[] code = writer.toByteArray();
            try {
                // 将二进制流写到本地磁盘上
                FileOutputStream fos = new FileOutputStream("/Users/jokerwan/Desktop/OutputHelloWorld.class");
                fos.write(code);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failure!");
        }
    }

    static class ChangeVisitor extends ClassVisitor {

        ChangeVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM5, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals("<init>")) {
                return methodVisitor;
            }
            return new ChangeAdapter(Opcodes.ASM4, methodVisitor, access, name, desc);
        }
    }

    static class ChangeAdapter extends AdviceAdapter {
        private int startTimeId = -1;

        private String methodName = null;

        ChangeAdapter(int api, MethodVisitor mv, int access, String name, String desc) {
            super(api, mv, access, name, desc);
            methodName = name;
        }

        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            startTimeId = newLocal(Type.LONG_TYPE);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitIntInsn(LSTORE, startTimeId);
        }

        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode);
            int durationId = newLocal(Type.LONG_TYPE);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitVarInsn(LLOAD, startTimeId);
            mv.visitInsn(LSUB);
            mv.visitVarInsn(LSTORE, durationId);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitLdcInsn("The cost time of " + methodName + "() is ");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitVarInsn(LLOAD, durationId);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
            mv.visitLdcInsn(" ms");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

        }
    }
}
```
#### 5.5通过单元测试执行`ASMTest.redefineHelloWorldClass();`
```
public class ExampleUnitTest {
    @Test
    public void testASM() {
        ASMTest.redefineHelloWorldClass();
    }
}
```
`OutputHelloWorld.class`已经输出到桌面
![](https://upload-images.jianshu.io/upload_images/9513946-bd328e9b025f89eb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/200)


将`OutputHelloWorld.class`拖到Android Studio中，Android Studio会将字节码文件反编译为java文件，反编译后的代码如下
![](https://upload-images.jianshu.io/upload_images/9513946-abf322f8e04e791e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


可以看到我们成功通过ASM将统计运行时长的代码插入到`sayHello()`方法中。

**demo代码如下**
[https://github.com/isJoker/ASM_Demo](https://github.com/isJoker/ASM_Demo)






参考文章
https://asm.ow2.io/developer-guide.html#classreader
https://www.ibm.com/developerworks/cn/java/j-lo-asm30/

