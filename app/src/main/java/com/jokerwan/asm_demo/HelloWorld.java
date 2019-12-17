package com.jokerwan.asm_demo;

/**
 * Created by JokerWan on 2019-12-17.
 * Function:
 */
public class HelloWorld {

    public void sayHello() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
