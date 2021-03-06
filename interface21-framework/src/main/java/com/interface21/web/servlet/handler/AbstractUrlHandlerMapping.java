/*
 * Copyright (c) 2011-2025 PiChen
 */

package com.interface21.web.servlet.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.interface21.web.util.WebUtils;
import com.interface21.beans.BeansException;
import com.interface21.context.ApplicationContextException;
import com.interface21.util.PathMatcher;

/**
 * Abstract base class for url-mapped HandlerMapping implementations.
 * Provides infrastructure for mapping handlers to URLs and configurable
 * URL lookup. For information on the latter, see alwaysUseFullPath property.
 * <p>
 * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
 * and a various Ant-style pattern matches, e.g. a registered "/t*" matches
 * both "/test" and "/team". For details, see the PathMatcher class.
 *
 * @author Juergen Hoeller
 * @see #setAlwaysUseFullPath
 * @see #setDefaultHandler
 * @see PathMatcher
 * @since 16.04.2003
 */
public abstract class AbstractUrlHandlerMapping extends AbstractHandlerMapping {

    private boolean alwaysUseFullPath = false;

    private Map handlerMap = new HashMap();

    /**
     * Set if URL lookup should always use full path within current servlet
     * context. Else, the path within the current servlet mapping is used
     * if applicable (i.e. in the case of a ".../*" servlet mapping in web.xml).
     * Default is false.
     */
    public final void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
        this.alwaysUseFullPath = alwaysUseFullPath;
    }

    /**
     * Register the given handler instance for the given URL path.
     *
     * @param urlPath URL the bean is mapped to
     * @param handler the handler instance
     */
    protected final void registerHandler(String urlPath, Object handler) {
        this.handlerMap.put(urlPath, handler);
        logger.info("Mapped URL path [" + urlPath + "] onto handler [" + handler + "]");
    }

    /**
     * Lookup a handler for the URL path of the given request.
     *
     * @param request current HTTP request
     * @return the looked up handler instance, or null
     */
    protected Object getHandlerInternal(HttpServletRequest request) {
        String lookupPath = WebUtils.getLookupPathForRequest(request, this.alwaysUseFullPath);
        logger.debug("Looking up handler for: " + lookupPath);
        return lookupHandler(lookupPath);
    }

    /**
     * Look up a handler instance for the given URL path.
     * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
     * and a various Ant-style pattern matches, e.g. a registered "/t*" matches
     * both "/test" and "/team". For details, see the PathMatcher class.
     *
     * @param urlPath URL the bean is mapped to
     * @return the associated handler instance, or null if not found
     * @see PathMatcher
     */
    protected final Object lookupHandler(String urlPath) {
        Object handler = this.handlerMap.get(urlPath);
        if (handler != null) {
            return handler;
        }
        for (Iterator it = this.handlerMap.keySet().iterator(); it.hasNext(); ) {
            String registeredPath = (String) it.next();
            if (PathMatcher.match(registeredPath, urlPath)) {
                return this.handlerMap.get(registeredPath);
            }
        }
        // no match found
        return null;
    }

    /**
     * Initialize the handler object with the given name in the bean factory.
     * This includes setting the LocaleResolver and mapped URL if aware.
     *
     * @param beanName name of the bean in the application context
     * @param urlPath  URL the bean is mapped to
     * @return the initialized handler instance
     * @throws ApplicationContextException if the bean wasn't found in the context
     */
    protected final Object initHandler(String beanName, String urlPath) throws ApplicationContextException {
        try {
            Object handler = getApplicationContext().getBean(beanName);
            logger.debug("Initializing handler [" + handler + "] for URL path [" + urlPath + "]");
            if (handler instanceof UrlAwareHandler) {
                ((UrlAwareHandler) handler).setUrlMapping(urlPath);
            }
            return handler;
        } catch (BeansException ex) {
            // We don't need to worry about NoSuchBeanDefinitionException:
            // we should have got the name from the bean factory.
            throw new ApplicationContextException("Error initializing handler bean for URL mapping '" + beanName + "': " + ex.getMessage(), ex);
        }
    }

}
