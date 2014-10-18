import logging
import psycopg2


logger = logging.getLogger('dbclient')

_connections = {}


def configure(db_host, db_port, db_name, db_username, db_password, autocommit, conn_name='default'):
    global _connections
    
    logger.debug('configuring connection with name=%(name)s host=%(host)s, port=%(port)s, dbname=%(dbname)s, username=%(username)s, autocommit=%(autocommit)s' % {
            'name': conn_name,
            'host': db_host,
            'port': db_port,
            'dbname': db_name,
            'username': db_username,
            'autocommit': autocommit})
    
    _connections[conn_name] = {
        'host': db_host,
        'port': db_port,
        'dbname': db_name,
        'username': db_username,
        'password': db_password,
        'autocommit': autocommit,
        'dbcon': None,
        'cursor': None 
    }


def get_connection(create=None, conn_name='default'):
    global _connections
    
    connection = _connections.get(conn_name)
    if not connection:
        logger.error('connection %(name)s not configured' % {'name': conn_name})
        
        return None
    
    if (create is True) or (connection['dbcon'] is None and create is not False):
        logger.info('creating a new db connection for %(name)s...' % {'name': conn_name})

        try:        
            dbcon = psycopg2.connect("host=%(host)s port=%(port)s dbname=%(dbname)s user=%(username)s password=%(password)s" % {
                    'host': connection['host'],
                    'port': connection['port'],
                    'dbname': connection['dbname'],
                    'username': connection['username'],
                    'password': connection['password']})
            
            try:
                dbcon.autocommit = connection['autocommit']
            
            except AttributeError: # older psycopg
                if connection['autocommit']:
                    dbcon.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT)
                
                else:
                    dbcon.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_READ_COMMITTED)
            
        except psycopg2.Error as e:
            logger.error('failed to connect to db for %(name)s: %(msg)s' % {
                    'name': conn_name,
                    'msg': str(e).strip()})
            
            dbcon = None

        connection['dbcon'] = dbcon
    
    return connection['dbcon']


def get_cursor(create=None, conn_name='default'):
    global _connections
    
    connection = _connections.get(conn_name)
    if not connection:
        logger.error('connection %(name)s not configured' % {'name': conn_name})
        
        return None

    if (create is True) or (connection['cursor'] is None and create is not False):
        dbconn = get_connection(conn_name=conn_name)
        
        logger.info('creating a new db cursor for %(name)s...' % {'name': conn_name})
        if dbconn:
            try:
                cursor = dbconn.cursor()
            
            except psycopg2.Error as e:
                logger.error('failed to create cursor: %(msg)s' % {'msg': str(e).strip()})
                
                cursor = None

        else:
            logger.error('failed to create cursor: no database connection')
            
            cursor = None
            
        connection['cursor'] = cursor

    return connection['cursor']


def sql(statement, commit=False, conn_name='default', **kwargs):
    connection = _connections.get(conn_name)
    if not connection:
        logger.error('connection %(name)s not configured' % {'name': conn_name})
        
        return False

    cursor = get_cursor(conn_name=conn_name)
    
    statement_str = statement % kwargs # only used for logging
    logger.debug('executing sql on %(name)s: "%(statement)s"...' % {
            'name': conn_name,
            'statement': statement_str})

    if cursor is None:
        logger.error('failed to execute sql for %(name)s: "%(statement)s": no cursor' % {
                'name': conn_name,
                'statement': statement_str})
        
        return False

    try:
        cursor.execute(statement, kwargs)
        
        if ((cursor.rowcount > 0) and
            (statement.lower().strip().startswith('select') or
             statement.lower().count('returning'))):

            result = cursor.fetchall()
        
        else:
            result = cursor.rowcount
            if result == 0 and statement.lower().strip().startswith('select'): # special 0 records result
                result = []

    except psycopg2.Error as e:
        logger.error('failed to execute sql for %(name)s: "%(statement)s": %(msg)s' % {
                'name': conn_name, 'statement': statement_str, 'msg': str(e).strip()})
        
        return False
    
    if commit:
        dbconn = get_connection(conn_name=conn_name)
        try:
            dbconn.commit()
        
        except psycopg2.Error as e:
            logger.error('failed to commit transaction for %(name)s: %(msg)s' % {
                    'name': conn_name,
                    'msg': str(e).strip()})
            
            return False
    
    return result


def commit(conn_name='default'):
    logger.debug('committing transaction for %(name)s...' % {'name': conn_name})

    dbconn = get_connection(create=False, conn_name=conn_name)
    if dbconn is None:
        logger.debug('no transaction to commit for %(name)s' % {'name': conn_name})
        
        return True

    try:
        dbconn.commit()
    
    except psycopg2.Error as e:
        logger.error('failed to commit transaction for %(name)s: %(msg)s' % {
                'name': conn_name,
                'msg': str(e).strip()})
        
        return False
    
    return True


def close():
    global _connections
    
    logger.debug('shutting down db module...')
    
    for conn_name, connection in _connections.items():
        if connection['cursor']:
            logger.info('closing db cursor for %(name)s...' % {'name': conn_name})
            connection['cursor'].close()

        if connection['dbcon']:
            logger.info('closing db connection for %(name)s...' % {'name': conn_name})
            connection['dbcon'].close()

