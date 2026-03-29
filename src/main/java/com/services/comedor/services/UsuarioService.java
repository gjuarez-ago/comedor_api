package com.services.comedor.services;

import com.services.comedor.entity.*;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.CrearUsuarioRequest;
import com.services.comedor.models.CrearUsuarioResponse;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {


}