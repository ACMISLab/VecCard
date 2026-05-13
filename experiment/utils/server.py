import paramiko
import time
import os


def get_server_time_by_config(config):
    container_config = config["server"]["container"]
    return get_server_time(container_config["host"], container_config["port"], container_config["user"],
                           container_config["password"])


def get_server_time(host, port, username, password):
    try:
        # 创建SSH对象
        ssh = paramiko.SSHClient()
        # 添加新的SSH密钥
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        # 连接服务器
        ssh.connect(host, port, username, password)
        # 执行命令获取服务器时间
        stdin, stdout, stderr = ssh.exec_command('date +%s')
        # 读取命令输出
        server_time = stdout.read().decode('utf-8').strip()
        # 关闭连接
        ssh.close()
        return int(server_time)
    except Exception as e:
        return str(e)


def restart_server(config, args):
    print("restart system and database ....")
    server_config = config["server"]
    master = server_config["master"]
    container = server_config["container"]
    print(master)
    print(container)
    master_ssh = paramiko.SSHClient()
    master_ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    master_ssh.connect(hostname=master["host"], username=master["user"], password=master["password"])

    # 重启docker 容器
    command = "echo '{}' | sudo -S {}".format(master["password"], "docker restart dingodb_" + args.config)
    stdin, stdout, stderr = master_ssh.exec_command(command)
    # 获取命令执行结果
    result = stdout.read().decode()
    print("container is restart: " + result)

    time.sleep(3)
    # 限制容器使用的资源
    dokcer_command = f'docker container update dingodb_{args.config} --cpus="{container["cpu"]}" --memory="{container["memory"]}" --memory-swap="{container["swap"]}"'
    command_1 = "echo '{}' | sudo -S {}".format(master["password"],
                                                dokcer_command)

    stdin_1, stdout_1, stderr_1 = master_ssh.exec_command(command_1)
    result_1 = stdout_1.read().decode()
    print("container resource limit: " + result_1)
    time.sleep(6)
    master_ssh.close()

    container_ssh = paramiko.SSHClient()
    container_ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    container_ssh.connect(hostname=container["host"], port=container["port"], username=container["user"],
                          password=container["password"])
    stdin, stdout, stderr = container_ssh.exec_command("rm -rf /root/java_pid*")
    result = stdout.read().decode()
    print("The redundant files in the system have been deleted: " + result)
    time.sleep(2)

    # 更改jvm内存
    sh_file_path = os.environ.get("VECCARD_EXECUTOR_SH", "/home/dingo/dingo-store/bin/start-executor.sh")
    new_memory_size = config["database"]["jvm_memory"]
    sed_command = f"sed -i 's/-Xms[0-9]\\+[kmg] -Xmx[0-9]\\+[kmg]/-Xms{new_memory_size} -Xmx{new_memory_size}/g' {sh_file_path}"

    container_ssh.exec_command(sed_command)
    time.sleep(1)

    # about prometheus
    if not config["prometheus"]["enable"]:
        container_ssh.exec_command("systemctl stop dingo-prometheus.service")
        container_ssh.exec_command("systemctl stop dingo-node-exporter")
        container_ssh.exec_command("systemctl stop dingo-process-exporter")
        time.sleep(2)

    container_ssh.exec_command(os.environ.get("VECCARD_RESTART_STORE_SH", "bash /root/script/restart-dingo-store.sh"))
    # result = stdout.read().decode()
    # print("container result step2: " + result)
    time.sleep(10)
    container_ssh.exec_command(os.environ.get("VECCARD_RESTART_EXECUTOR_SH", "bash /root/script/restart-dingo-exector.sh"))
    # result = stdout.read().decode()
    # print("container result step3: " + result)
    time.sleep(10)
    print("Restart completed!!!")
    # 关闭连接
    container_ssh.close()
