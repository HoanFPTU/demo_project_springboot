//package com.example.starter_project;
//
//
//import com.example.starter_project.systems.controller.RoleController;
//import com.example.starter_project.systems.dto.RoleDTO;
//import com.example.starter_project.systems.entity.Role;
//import com.example.starter_project.systems.service.RoleService;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.io.ByteArrayOutputStream;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(RoleController.class)
//public class RoleControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private RoleService roleService;
//
//    @Test
//    void testCreateRoleSuccess() throws Exception {
//        RoleDTO dto = new RoleDTO();
//        dto.setName("Admin");
//
//        Mockito.when(roleService.findByName("Admin")).thenReturn(Optional.empty());
//        Mockito.when(roleService.create(any(RoleDTO.class))).thenReturn(dto);
//
//        mockMvc.perform(post("/api/roles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"name\": \"Admin\"}"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Admin"));
//    }
//
//    @Test
//    void testCreateRoleNameEmpty() throws Exception {
//        mockMvc.perform(post("/api/roles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"name\": \"\"}"))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Error: Role name cannot be null or empty."));
//    }
//
//    @Test
//    void testCreateRoleNameNullBeanValidation() throws Exception {
//        mockMvc.perform(post("/api/roles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{}"))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void testCreateRoleAlreadyExists() throws Exception {
//        Role existingRole = new Role();
//        existingRole.setId(1L);
//        existingRole.setName("Admin");
//
//        Mockito.when(roleService.findByName("Admin")).thenReturn(Optional.of(existingRole));
//
//        mockMvc.perform(post("/api/roles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"name\": \"Admin\"}"))
//                .andExpect(status().isConflict())
//                .andExpect(content().string("Error: Role with name 'Admin' already exists."));
//    }
//
//    @Test
//    void testGetAllWithoutSearch() throws Exception {
//        Page<RoleDTO> page = new PageImpl<>(
//                Collections.singletonList(new RoleDTO(null,"Admin")),
//                PageRequest.of(0, 10),
//                1
//        );
//        Mockito.when(roleService.findAllPaged(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);
//
//        mockMvc.perform(get("/api/roles"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("content[0].name").value("Admin"));
//    }
//
//    @Test
//    void testGetAllWithSearch() throws Exception {
//        Page<RoleDTO> page = new PageImpl<>(
//                Collections.singletonList(new RoleDTO(null,"User")),
//                PageRequest.of(0, 10),
//                1
//        );
//        Mockito.when(roleService.searchRoles(anyString(), anyInt(), anyInt(), anyString(), anyString()))
//                .thenReturn(page);
//
//        mockMvc.perform(get("/api/roles").param("search", "User"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("content[0].name").value("User"));
//    }
//
//    @Test
//    void testUpdateRoleSuccess() throws Exception {
//        Long id = 1L;
//        RoleDTO dto = new RoleDTO(null,"Manager");
//
//        Role existingRole = new Role();
//        existingRole.setId(1L);
//        existingRole.setName("Manager");
//
//        Mockito.when(roleService.findById(id)).thenReturn(Optional.of(existingRole));
//        Mockito.when(roleService.findByName("Manager")).thenReturn(Optional.of(existingRole));
//        Mockito.when(roleService.update(eq(id), any(RoleDTO.class))).thenReturn(dto);
//
//        mockMvc.perform(put("/api/roles/{id}", id)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"name\": \"Manager\"}"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Manager"));
//    }
//
//    @Test
//    void testUpdateRoleNotFound() throws Exception {
//        Mockito.when(roleService.findById(100L)).thenReturn(Optional.empty());
//
//        mockMvc.perform(put("/api/roles/{id}", 100)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"name\": \"Manager\"}"))
//                .andExpect(status().isNotFound())
//                .andExpect(content().string("Error: Role with ID '100' not found."));
//    }
//
//    @Test
//    void testDeleteRole() throws Exception {
//        mockMvc.perform(delete("/api/roles/{id}", 1))
//                .andExpect(status().isNoContent());
//        Mockito.verify(roleService).deleteRole(1L);
//    }
//
//    @Test
//    void testImportExcelSuccess() throws Exception {
//        // Create Excel file in memory
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        var workbook = WorkbookFactory.create(true); // true = XSSF
//        var sheet = workbook.createSheet();
//        var header = sheet.createRow(0);
//        header.createCell(0).setCellValue("Name");
//        var row1 = sheet.createRow(1);
//        row1.createCell(0).setCellValue("Admin");
//
//        workbook.write(out);
//        workbook.close();
//
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "roles.xlsx",
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//                out.toByteArray()
//        );
//
//        Mockito.when(roleService.findByName("Admin")).thenReturn(Optional.empty());
//
//        mockMvc.perform(multipart("/api/roles/import").file(file))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Roles imported successfully."));
//    }
//
//    @Test
//    void testExportExcel() throws Exception {
//        Mockito.when(roleService.findAll()).thenReturn(
//                Arrays.asList(new RoleDTO(null,"Admin"), new RoleDTO(null,"User"))
//        );
//
//        mockMvc.perform(get("/api/roles/export"))
//                .andExpect(status().isOk())
//                .andExpect(header().string("Content-Disposition", "attachment; filename=roles.xlsx"));
//    }
//
//    @Test
//    void testGetRoleCountWithoutSearch() throws Exception {
//        Mockito.when(roleService.countRoles()).thenReturn(5L);
//
//        mockMvc.perform(get("/api/roles/count"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("5"));
//    }
//
//    @Test
//    void testGetRoleCountWithSearch() throws Exception {
//        Mockito.when(roleService.countBySearchCriteria("admin")).thenReturn(2L);
//
//        mockMvc.perform(get("/api/roles/count").param("search", "admin"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("2"));
//    }
//}
