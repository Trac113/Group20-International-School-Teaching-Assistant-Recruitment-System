package com.qq.recruitment;

/**
 * Thin wrapper entry point for the fat JAR.
 * Delegates to App.main() to bypass JavaFX module path issues when running from a JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
