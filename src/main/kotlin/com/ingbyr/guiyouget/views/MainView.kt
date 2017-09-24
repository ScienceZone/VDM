package com.ingbyr.guiyouget.views

import com.ingbyr.guiyouget.controllers.MainController
import com.ingbyr.guiyouget.events.RequestCheckUpdatesYouGet
import com.ingbyr.guiyouget.events.RequestCheckUpdatesYoutubeDL
import com.ingbyr.guiyouget.utils.CoreUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXTextField
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.stage.DirectoryChooser
import javafx.stage.StageStyle
import tornadofx.*
import java.nio.file.Paths

class MainView : View("GUI-YouGet") {
    private var xOffset = 0.0
    private var yOffset = 0.0

    override val root: AnchorPane by fxml("/fxml/MainWindow.fxml")

    private val controller: MainController by inject()

    private val tfURL: JFXTextField by fxid()

    private val labelStoragePath: Label by fxid()
    private val labelCoreVersion: Label by fxid()
    private val labelVersion: Label by fxid()
    private val labelYoutubeDL: Label by fxid()
    private val labelYouGet: Label by fxid()

    private val btnDownload: JFXButton by fxid()
    private val btnChangePath: JFXButton by fxid()
    private val btnUpdateCore: JFXButton by fxid()
    private val btnUpdate: JFXButton by fxid()

    private val cbYoutubeDL: JFXCheckBox by fxid()
    private val cbYouGet: JFXCheckBox by fxid()

    private val paneExit: Pane by fxid()
    private val apBorder: AnchorPane by fxid()

    init {
        // Window boarder
        primaryStage.initStyle(StageStyle.UNDECORATED)
        paneExit.setOnMouseClicked {
            Platform.exit()
        }

        apBorder.setOnMousePressed { event: MouseEvent? ->
            event?.let {
                xOffset = event.sceneX
                yOffset = event.sceneY
            }
        }

        apBorder.setOnMouseDragged { event: MouseEvent? ->
            event?.let {
                primaryStage.x = event.screenX - xOffset
                primaryStage.y = event.screenY - yOffset
            }
        }

        // Storage path
        if (app.config["storagePath"] == null) {
            labelStoragePath.text = Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString()
            app.config["storagePath"] = labelStoragePath.text
            app.config.save()
        } else {
            labelStoragePath.text = app.config["storagePath"] as String
        }

        btnChangePath.setOnMouseClicked {
            val file = DirectoryChooser().showDialog(primaryStage)
            if (file != null) {
                app.config["storagePath"] = file.absolutePath.toString()
                app.config.save()
                labelStoragePath.text = file.absolutePath.toString()
            }
        }

        // Get media list
        btnDownload.setOnMouseClicked {
            if (tfURL.text != null && tfURL.text.trim() != "") {
                replaceWith(MediaListView::class, ViewTransition.Slide(0.3.seconds, ViewTransition.Direction.LEFT))
                controller.requestMediaInfo(tfURL.text)
            }
        }

        // Load download core config
        val core = app.config["core"]
        when (core) {
            CoreUtils.YOUTUBE_DL -> {
                cbYoutubeDL.isSelected = true
                CoreUtils.current = CoreUtils.YOUTUBE_DL
            }
            CoreUtils.YOU_GET -> {
                cbYouGet.isSelected = true
                CoreUtils.current = CoreUtils.YOU_GET
            }
            else -> {
                app.config["core"] = CoreUtils.YOUTUBE_DL
                app.config.save()
                cbYoutubeDL.isSelected = true
                CoreUtils.current = CoreUtils.YOUTUBE_DL
            }
        }

        // Load core version
        labelYouGet.text = app.config["you-get-version"] as String
        labelYoutubeDL.text = app.config["youtube-dl-version"] as String

        // Change download core
        cbYouGet.action {
            if (cbYouGet.isSelected) {
                cbYoutubeDL.isSelected = false
                app.config["core"] = CoreUtils.YOU_GET
                app.config.save()
            }
        }

        cbYoutubeDL.action {
            if (cbYoutubeDL.isSelected) {
                cbYouGet.isSelected = false
                app.config["core"] = CoreUtils.YOUTUBE_DL
                app.config.save()
            }
        }

        // Updates listener
        btnUpdateCore.setOnMouseClicked {
            UpdatesWindow().openModal(StageStyle.UNDECORATED)
            fire(RequestCheckUpdatesYouGet)
            fire(RequestCheckUpdatesYoutubeDL)
        }

        btnUpdate.setOnMouseClicked {
            controller.updateGUI()
        }
    }

    // clean the url textfield
    override fun onDock() {
        tfURL.text = ""
    }
}