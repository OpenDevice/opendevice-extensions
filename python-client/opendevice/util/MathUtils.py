
def maprange(left_min, left_max, right_min, right_max):
    # Figure out how 'wide' each range is
    leftSpan = left_max - left_min
    rightSpan = right_max - right_min

    # Compute the scale factor between left and right values
    factor = float(rightSpan) / float(leftSpan)

    # create interpolation function using pre-calculated factor
    def interp_fn(value):
        return right_min + (value-left_min)*factor

    return interp_fn



def maprange_f(left_min, left_max, right_min, right_max):
    """
    Usage: maprange( (0, 10), (-1, 0), value)
    """
    # Figure out how 'wide' each range is
    leftSpan = left_max - left_min
    rightSpan = right_max - right_min

    # Compute the scale factor between left and right values
    factor = float(rightSpan) / float(leftSpan)

    # create interpolation function using pre-calculated factor
    def interp_fn(value):
        return right_min + (value-left_min)*factor

    return interp_fn
