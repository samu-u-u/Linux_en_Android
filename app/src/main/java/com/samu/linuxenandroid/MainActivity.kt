package com.samu.linuxenandroid

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.samu.linuxenandroid.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val distros = Distro.ALL
    private var installedDistros: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            distros.map { it.label },
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDistro.adapter = adapter
        binding.spinnerDistro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateButtons()
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        CommandResultReceiver.listener = { result -> runOnUiThread { handleResult(result) } }

        binding.btnRefresh.setOnClickListener {
            refreshEnvironment()
            refreshInstalledDistros()
        }
        binding.btnInstallDistro.setOnClickListener { installDistro() }
        binding.btnInstallGui.setOnClickListener { installGui() }
        binding.btnLaunch.setOnClickListener { launchGui() }
        binding.btnOpenTermux.setOnClickListener {
            openApp(TermuxBridge.TERMUX_PACKAGE, R.string.toast_need_termux)
        }
        binding.btnOpenX11.setOnClickListener {
            openApp(TermuxBridge.TERMUX_X11_PACKAGE, R.string.toast_need_x11)
        }

        refreshEnvironment()
        refreshInstalledDistros()
    }

    override fun onDestroy() {
        CommandResultReceiver.listener = null
        super.onDestroy()
    }

    private fun selectedDistro(): Distro = distros[binding.spinnerDistro.selectedItemPosition]

    private fun refreshEnvironment() {
        val termuxOk = TermuxBridge.isAppInstalled(this, TermuxBridge.TERMUX_PACKAGE)
        val x11Ok = TermuxBridge.isAppInstalled(this, TermuxBridge.TERMUX_X11_PACKAGE)
        binding.txtStatusTermux.text = getString(R.string.status_termux, stateText(termuxOk))
        binding.txtStatusTermux.setTextColor(stateColor(termuxOk))
        binding.txtStatusX11.text = getString(R.string.status_x11, stateText(x11Ok))
        binding.txtStatusX11.setTextColor(stateColor(x11Ok))
        updateButtons()
    }

    private fun stateText(ok: Boolean) = getString(if (ok) R.string.state_installed else R.string.state_missing)

    private fun stateColor(ok: Boolean) =
        Color.parseColor(if (ok) "#2E7D32" else "#C62828")

    private fun refreshInstalledDistros() {
        installedDistros = emptySet()
        updateButtons()
        if (!TermuxBridge.isAppInstalled(this, TermuxBridge.TERMUX_PACKAGE)) return
        log(getString(R.string.log_listing))
        if (!TermuxBridge.runProotDistro(this, arrayOf("list"), LABEL_LIST)) {
            onCommandNotSent()
        }
    }

    private fun installDistro() {
        val distro = selectedDistro()
        log(getString(R.string.log_installing, distro.image))
        val script = "pkg install -y proot-distro && proot-distro install ${distro.image}"
        if (!TermuxBridge.runShell(this, script, LABEL_INSTALL)) onCommandNotSent()
    }

    private fun installGui() {
        val distro = selectedDistro()
        log(getString(R.string.log_install_gui, distro.alias))
        val args = arrayOf(
            "login", distro.alias, "--shared-tmp", "--",
            "/bin/bash", "-c", distro.guiInstallCommand,
        )
        if (!TermuxBridge.runProotDistro(this, args, LABEL_GUI)) onCommandNotSent()
    }

    private fun launchGui() {
        val distro = selectedDistro()
        log(getString(R.string.log_launch, distro.alias))
        if (TermuxBridge.isAppInstalled(this, TermuxBridge.TERMUX_X11_PACKAGE)) {
            TermuxBridge.launchApp(this, TermuxBridge.TERMUX_X11_PACKAGE)
        } else {
            Toast.makeText(this, R.string.toast_need_x11, Toast.LENGTH_LONG).show()
        }
        val args = arrayOf(
            "login", distro.alias, "--shared-tmp", "--shared-x11", "--",
            "/bin/bash", "-c", distro.guiLaunchCommand,
        )
        if (!TermuxBridge.runProotDistro(this, args, LABEL_LAUNCH)) onCommandNotSent()
    }

    private fun openApp(packageName: String, missingToast: Int) {
        if (!TermuxBridge.isAppInstalled(this, packageName)) {
            Toast.makeText(this, missingToast, Toast.LENGTH_LONG).show()
            return
        }
        TermuxBridge.launchApp(this, packageName)
    }

    private fun handleResult(result: CommandResult) {
        if (result.label == LABEL_LIST) {
            val stdout = result.stdout.orEmpty()
            if (result.exitCode == 0) {
                installedDistros = distros
                    .filter { distro -> stdout.lines().any { it.contains(distro.alias) } }
                    .map { it.alias }
                    .toSet()
            }
            val names = installedDistros.joinToString(", ").ifEmpty { getString(R.string.none) }
            log(getString(R.string.installed_distros, names))
            updateButtons()
            return
        }
        if (result.err != 0 && !result.errmsg.isNullOrBlank()) {
            log(getString(R.string.log_result_err, result.label, result.errmsg))
        }
        val output = listOfNotNull(
            result.stdout?.takeLast(2000),
            result.stderr?.takeLast(2000),
        ).joinToString("\n")
        if (output.isNotBlank()) log("[${result.label}]\n$output")
        log(getString(R.string.log_result_ok, result.label, result.exitCode))
        if (result.label == LABEL_INSTALL) refreshInstalledDistros()
    }

    private fun updateButtons() {
        val termuxOk = TermuxBridge.isAppInstalled(this, TermuxBridge.TERMUX_PACKAGE)
        val installed = installedDistros.contains(selectedDistro().alias)
        binding.btnInstallDistro.isEnabled = termuxOk
        binding.btnInstallGui.isEnabled = termuxOk && installed
        binding.btnLaunch.isEnabled = termuxOk && installed
    }

    private fun onCommandNotSent() {
        binding.txtHint.visibility = View.VISIBLE
        Toast.makeText(this, R.string.toast_command_not_sent, Toast.LENGTH_LONG).show()
    }

    private fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        binding.txtLog.append("[$timestamp] $message\n")
        binding.logScroll.post { binding.logScroll.fullScroll(View.FOCUS_DOWN) }
    }

    companion object {
        private const val LABEL_LIST = "list"
        private const val LABEL_INSTALL = "install"
        private const val LABEL_GUI = "gui-install"
        private const val LABEL_LAUNCH = "launch"
    }
}
