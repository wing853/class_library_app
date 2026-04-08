package com.tenco.library;

import com.tenco.library.view.LibraryView;
import com.tenco.library.view.swing.LibraryViewSwing;

import javax.swing.*;

public class Main {

    // 프로그램 시작점
    public static void main(String[] args) {
        LibraryView libraryView = new LibraryView();
        libraryView.start();
//        SwingUtilities.invokeLater(() -> {
//            new LibraryViewSwing().setVisible(true);
//        });

    }

}