package tcpforwarder.tcpforwarder.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    // Forward all non-file requests to index.html so React Router works
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String spa() {
        return "forward:/index.html";
    }
}
