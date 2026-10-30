package com.bazaar.subastas.puertos;

import java.util.Collection;
import java.util.Map;

public interface UsuarioPort {
    Long obtenerIdPorEmail(String email);
    Map<Long, String> nombresPorId(Collection<Long> ids);

}