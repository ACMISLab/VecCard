import multiprocessing
import psutil
import time
import math
import sys

# 获取系统资源总量
TOTAL_CPU = psutil.cpu_count(logical=True)
TOTAL_MEM = psutil.virtual_memory().total

# 目标资源占用率
TARGET_CPU = 0.6  # 60%
TARGET_MEM = 0.6  # 60%

def cpu_worker():
    """固定占用单核60%的CPU"""
    interval = 0.6  # 工作时间占周期比例
    period = 1.0    # 完整周期时间（秒）
    
    while True:
        start = time.time()
        # 计算密集型工作
        while time.time() - start < interval:
            math.factorial(500)
        # 休眠剩余时间
        time.sleep(period - interval)

def mem_worker():
    """固定占用60%物理内存"""
    block_size = 100 * 1024 * 1024  # 100MB/块
    target_mem = int(TOTAL_MEM * TARGET_MEM)
    mem_blocks = []
    
    try:
        # 分块分配内存
        while psutil.virtual_memory().available > (TOTAL_MEM - target_mem):
            mem_blocks.append(b'0' * block_size)
            time.sleep(0.01)
    except MemoryError:
        pass
    
    # 保持内存占用
    while True:
        time.sleep(1)

def cleanup():
    """清理资源"""
    for p in multiprocessing.active_children():
        p.terminate()
    print("\n资源已释放")

if __name__ == "__main__":
    try:
        print(f"启动资源占用（CPU：{TARGET_CPU*100}%，内存：{TARGET_MEM*100}%）")
        print("按 Ctrl+C 停止\n")

        # 启动内存占用
        multiprocessing.Process(target=mem_worker).start()

        # 启动CPU占用（每个逻辑核心一个进程）
        for _ in range(TOTAL_CPU):
            multiprocessing.Process(target=cpu_worker).start()

        # 保持主进程运行
        while True:
            time.sleep(10)
            
    except KeyboardInterrupt:
        cleanup()
