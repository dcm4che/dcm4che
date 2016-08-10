package org.dcm4che3.conf.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.*;
import javax.naming.directory.*;
import java.io.Closeable;
import java.util.Hashtable;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Jan 2016
 */
class ReconnectDirContext implements Closeable {

    static final Logger LOG = LoggerFactory.getLogger(ReconnectDirContext.class);

    private final Hashtable env;

    private DirContext ctx;

    public DirContext getDirCtx() {
        return ctx;
    }

    public ReconnectDirContext(Hashtable<?,?> env) throws NamingException {
        this.env = (Hashtable) env.clone();
        this.ctx = new InitialDirContext(env);
    }

    private void reconnect() throws NamingException {
        LOG.info("Connection to {} broken - reconnect", env.get(Context.PROVIDER_URL));
        close();
        ctx = new InitialDirContext(env);
    }

    @Override
    public void close() {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException ignore) {}
            ctx = null;
        }
    }

    public Attributes getAttributes(String name) throws NamingException {
        try {
            return ctx.getAttributes(name);
        } catch (CommunicationException e) {
            reconnect();
            return ctx.getAttributes(name);
        }
    }

    public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
        try {
            return ctx.getAttributes(name, attrIds);
        } catch (CommunicationException e) {
            reconnect();
            return ctx.getAttributes(name, attrIds);
        }
    }

    public void destroySubcontext(String name) throws NamingException {
        try {
            ctx.destroySubcontext(name);
        } catch (CommunicationException e) {
            reconnect();
            ctx.destroySubcontext(name);
        }
    }

    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
            throws NamingException {
        try {
            return ctx.search(name, filter, cons);
        } catch (CommunicationException e) {
            reconnect();
            return ctx.search(name, filter, cons);
        }
    }

    public void createSubcontextAndClose(String name, Attributes attrs) throws NamingException {
        try {
            ctx.createSubcontext(name, attrs).close();
        } catch (CommunicationException e) {
            reconnect();
            ctx.createSubcontext(name, attrs).close();
        }
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        try {
            return ctx.list(name);
        } catch (CommunicationException e) {
            reconnect();
            return ctx.list(name);
        }
    }

    public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
        try {
            ctx.modifyAttributes(name, mods);
        } catch (CommunicationException e) {
            reconnect();
            ctx.modifyAttributes(name, mods);
        }
    }

    public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException {
        try {
            ctx.modifyAttributes(name, mod_op, attrs);
        } catch (CommunicationException e) {
            reconnect();
            ctx.modifyAttributes(name, mod_op, attrs);
        }
    }
}
