def success(extra=None):
    ret = {
        'success': True,
        'error_msg': ''
    }
    if extra is not None:
        ret.update(extra)
    return ret


def fail(msg):
    return {
        'success': False,
        'error_msg': msg
    }


def field_required(field):
    return {
        'success': False,
        'error_msg': '{} is required'.format(field)
    }


def permission_denied():
    return {
        'success': False,
        'error_msg': 'Permission denied'
    }
