package org.timeflow.service;

import org.timeflow.dao.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseService {
    protected static final Logger logger = LoggerFactory.getLogger(BaseService.class);
    protected final DAOFactory daoFactory;

    public BaseService() {
        this.daoFactory = DAOFactory.getInstance();
    }
}

