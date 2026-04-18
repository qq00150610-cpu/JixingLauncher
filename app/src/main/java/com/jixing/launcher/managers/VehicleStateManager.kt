package com.jixing.launcher.managers

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jixing.launcher.model.VehicleState
import com.jixing.launcher.model.GearPosition
import com.jixing.launcher.model.DoorStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

// Car service constant
private const val CAR_SERVICE = "car_service"

/**
 * 车辆状态管理器
 * 支持车载环境检测和非车载环境模拟
 */
class VehicleStateManager private constructor(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    private val _vehicleState = MutableStateFlow(VehicleState())
    val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()
    
    private val _isDriving = MutableStateFlow(false)
    val isDriving: StateFlow<Boolean> = _isDriving.asStateFlow()

    private val _isAutomotiveEnvironment = MutableStateFlow(false)
    val isAutomotiveEnvironment: StateFlow<Boolean> = _isAutomotiveEnvironment.asStateFlow()

    private var isMonitoring = false

    // 模拟数据参数
    private var simulationSpeed: Float = 0f
    private var simulationDirection: Float = 1f // 速度变化方向

    init {
        checkAutomotiveEnvironment()
        initializeSimulationData()
    }

    /**
     * 检测是否为车载环境
     */
    private fun checkAutomotiveEnvironment() {
        val isAutomotive = try {
            // 检查是否安装了 automotive 相关的包
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check automotive feature", e)
            false
        }

        // 也检查是否存在车载服务
        val hasCarService = try {
            context.getSystemService(CAR_SERVICE) != null
        } catch (e: Exception) {
            false
        }

        _isAutomotiveEnvironment.value = isAutomotive || hasCarService
        
        Log.i(TAG, "Automotive environment check: isAutomotive=$isAutomotive, hasCarService=$hasCarService")
    }

    /**
     * 初始化模拟数据
     */
    private fun initializeSimulationData() {
        if (_isAutomotiveEnvironment.value) {
            // 车载环境：初始速度为0（停车状态）
            simulationSpeed = 0f
        } else {
            // 非车载环境：使用模拟数据，从静止开始
            simulationSpeed = 0f
            _vehicleState.value = createDefaultSimulationState()
        }
    }

    /**
     * 创建默认模拟状态
     */
    private fun createDefaultSimulationState(): VehicleState {
        return VehicleState(
            speed = 0f,
            rpm = 800f, // 怠速转速
            fuelLevel = 75,
            batteryVoltage = 12.6f,
            engineTemperature = 90,
            gear = GearPosition.PARK,
            doorStatus = DoorStatus(),
            isDriving = false
        )
    }

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        // 重新检测车载环境
        checkAutomotiveEnvironment()
        
        updateRunnable = object : Runnable {
            override fun run() {
                updateVehicleState()
                handler.postDelayed(this, UPDATE_INTERVAL)
            }
        }
        handler.post(updateRunnable!!)
        
        Log.i(TAG, "Vehicle state monitoring started, isAutomotive=${_isAutomotiveEnvironment.value}")
    }

    fun stopMonitoring() {
        isMonitoring = false
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        Log.i(TAG, "Vehicle state monitoring stopped")
    }

    private fun updateVehicleState() {
        // 车载环境：尝试从真实车载 API 获取数据
        // 非车载环境：使用模拟数据
        if (_isAutomotiveEnvironment.value) {
            try {
                updateFromRealVehicleAPI()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get real vehicle data, falling back to simulation", e)
                updateFromSimulation()
            }
        } else {
            updateFromSimulation()
        }
        
        _isDriving.value = _vehicleState.value.isDriving
    }

    /**
     * 从真实车载 API 获取数据
     */
    private fun updateFromRealVehicleAPI() {
        // 实际项目中应该通过 CarService 获取真实数据
        // 这里作为占位符，真实环境需要实现具体的 API 调用
        // 例如：CarPropertyManager.getProperty()
        updateFromSimulation()
    }

    /**
     * 使用模拟数据更新车辆状态
     */
    private fun updateFromSimulation() {
        val currentState = _vehicleState.value
        
        // 更新模拟速度
        simulationSpeed = simulateSpeed(simulationSpeed)
        
        // 计算模拟 RPM
        val rpm = calculateRpm(simulationSpeed, currentState.gear)
        
        // 随机燃油消耗（非常缓慢）
        val fuelLevel = if (simulationSpeed > 0 && Random.nextFloat() < 0.001f) {
            (currentState.fuelLevel - 1).coerceIn(0, 100)
        } else {
            currentState.fuelLevel
        }

        _vehicleState.value = currentState.copy(
            speed = simulationSpeed,
            rpm = rpm,
            fuelLevel = fuelLevel,
            batteryVoltage = simulateBatteryVoltage(currentState.batteryVoltage),
            engineTemperature = simulateEngineTemp(currentState.engineTemperature),
            gear = determineGear(simulationSpeed),
            doorStatus = getDoorStatus(),
            isDriving = simulationSpeed > 1f // 速度大于1km/h时认为在行驶
        )
    }

    /**
     * 模拟速度变化
     * 速度会在0-120km/h之间平滑变化
     */
    private fun simulateSpeed(current: Float): Float {
        return when {
            // 停止状态
            current < 1f -> {
                // 10%概率开始移动
                if (Random.nextFloat() < 0.1f) {
                    simulationDirection = 1f
                    5f
                } else {
                    0f
                }
            }
            // 加速阶段
            current < 60f -> {
                current + Random.nextFloat() * 3
            }
            // 高速行驶
            current < 100f -> {
                current + Random.nextFloat() * 2 * simulationDirection
            }
            // 接近最高速，概率性减速
            else -> {
                if (Random.nextFloat() < 0.3f) {
                    simulationDirection = -1f
                }
                (current - Random.nextFloat() * 2).coerceAtLeast(0f)
            }
        }.coerceIn(0f, 120f)
    }

    /**
     * 根据速度和档位计算 RPM
     */
    private fun calculateRpm(speed: Float, gear: GearPosition): Float {
        val gearRatio = when (gear) {
            GearPosition.PARK, GearPosition.NEUTRAL -> 0f
            GearPosition.REVERSE -> -0.5f
            GearPosition.DRIVE -> 1f
            GearPosition.SPORT -> 1.2f
            GearPosition.LOW -> 0.8f
        }
        
        return if (gear == GearPosition.PARK || gear == GearPosition.NEUTRAL) {
            800f + Random.nextFloat() * 100 // 怠速
        } else {
            (speed * 50 * gearRatio + 800 + Random.nextFloat() * 150).coerceIn(800f, 7000f)
        }
    }

    private fun simulateBatteryVoltage(current: Float): Float {
        // 电池电压在12.4-13.5V之间变化，模拟负载
        val variation = Random.nextFloat() * 0.3f - 0.1f
        return (current + variation).coerceIn(12.0f, 14.0f)
    }

    private fun simulateEngineTemp(current: Int): Int {
        // 温度在85-105度之间，模拟发动机热管理
        val variation = Random.nextInt(-2, 3)
        return (current + variation).coerceIn(80, 110)
    }

    private fun determineGear(speed: Float): GearPosition {
        return when {
            speed <= 0f -> GearPosition.PARK
            speed < 5f -> GearPosition.NEUTRAL
            speed < 30f -> GearPosition.DRIVE
            else -> GearPosition.DRIVE
        }
    }

    private fun getDoorStatus(): DoorStatus {
        // 模拟：行驶时所有车门关闭
        return if (_vehicleState.value.isDriving) {
            DoorStatus()
        } else {
            // 停车时随机车门状态（简化模拟）
            DoorStatus(
                frontLeft = Random.nextBoolean() && Random.nextFloat() < 0.1f,
                frontRight = Random.nextBoolean() && Random.nextFloat() < 0.1f,
                rearLeft = Random.nextBoolean() && Random.nextFloat() < 0.1f,
                rearRight = Random.nextBoolean() && Random.nextFloat() < 0.1f
            )
        }
    }

    /**
     * 手动设置模拟速度（用于测试）
     */
    fun setSimulatedSpeed(speed: Float) {
        simulationSpeed = speed.coerceIn(0f, 120f)
        updateFromSimulation()
    }

    /**
     * 获取是否为车载环境
     */
    fun isInAutomotiveEnvironment(): Boolean = _isAutomotiveEnvironment.value

    /**
     * 重置模拟数据
     */
    fun resetSimulation() {
        simulationSpeed = 0f
        simulationDirection = 1f
        _vehicleState.value = createDefaultSimulationState()
    }

    fun cleanup() {
        stopMonitoring()
    }

    companion object {
        private const val TAG = "VehicleStateManager"
        private const val UPDATE_INTERVAL = 1000L // 1秒更新一次
        
        @Volatile
        private var instance: VehicleStateManager? = null

        fun getInstance(context: Context? = null): VehicleStateManager {
            return instance ?: synchronized(this) {
                instance ?: context?.let {
                    VehicleStateManager(it.applicationContext).also { manager ->
                        instance = manager
                    }
                } ?: throw IllegalStateException("VehicleStateManager not initialized")
            }
        }

        /**
         * 重置单例实例（用于测试）
         */
        fun resetInstance() {
            instance?.cleanup()
            instance = null
        }
    }
}
