package cn.boen.uicab.service;

import cn.boen.uicab.entity.Department;
import cn.boen.uicab.mapper.DepartmentMapper;
import cn.boen.uicab.statics.EventType;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @OnEvent(EventType.GET_DEPT)
    private void getDept(SocketIOClient client) {
        try {
            client.sendEvent(EventType.GET_DEPT, departmentMapper.getAll());
        }catch (Exception e) {
            client.sendEvent(EventType.ERROR_MESSAGE, e.getMessage());
        }
    }

    @OnEvent(EventType.ADD_DEPT)
    private void addDept(SocketIOClient client, Department department) {
        try {
            if(departmentMapper.insert(department)) {
                client.sendEvent(EventType.ADD_DEPT);
            }else  {
                client.sendEvent(EventType.ERROR_MESSAGE, "参数错误，添加部门失败");
            }
        }catch (Exception e) {
            client.sendEvent(EventType.ERROR_MESSAGE, e.getMessage());
        }
    }

    @OnEvent(EventType.UPDATE_DEPT)
    private void updateDept(SocketIOClient client, Department department) {
        try {
            if(departmentMapper.update(department)) {
                client.sendEvent(EventType.UPDATE_DEPT);
            } else  {
                client.sendEvent(EventType.ERROR_MESSAGE, "参数错误，修改失败");
            }
        }catch (Exception e) {
            client.sendEvent(EventType.ERROR_MESSAGE, e.getMessage());
        }
    }

    @OnEvent(EventType.REMOVE_DEPT)
    private void removeDept(SocketIOClient client, Department department) {
        try {
            if(departmentMapper.delete(department.getId())) {
                client.sendEvent(EventType.REMOVE_DEPT);
            } else  {
                client.sendEvent(EventType.ERROR_MESSAGE, "参数错误，删除失败");
            }
        }catch (Exception e) {
            client.sendEvent(EventType.ERROR_MESSAGE, e.getMessage());
        }
    }
}
